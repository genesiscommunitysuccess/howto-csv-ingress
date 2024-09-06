package howto.csv.ingress.copp.clark.transform

import global.genesis.db.entity.TableEntity
import global.genesis.db.rx.entity.multi.AsyncEntityDb
import global.genesis.gen.dao.Exchange
import global.genesis.gen.dao.ExchangeHoliday
import global.genesis.gen.dao.enums.howto_csv_ingress.exchange_holiday.HolidayType
import global.genesis.pipeline.api.StreamOperator
import global.genesis.pipeline.api.db.DbOperation
import global.genesis.pipeline.file.CsvRow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This Transformer is very similar to CoppClarkHolidayTransformer, but demonstrates how we can return multiple different
 * db entity updates in one. In this case we have EXCHANGE entities as well as the EXCHANGE_HOLIDAYS
 *
 * See and understand fully commented CoppClarkHolidayTransformer first. Comments in this file are the differences only
 *
 * StreamOperator is different here, if we want 2+ different db entity operations returned we use `out TableEntity`
 */
class CoppClarkHolidayAndExchangeTransformer(
    private val entityDb: AsyncEntityDb,
    private val holidayType: HolidayType,
) : StreamOperator<CsvRow<Map<String, String>>, DbOperation<out TableEntity>> {

    companion object {
        val log: Logger = LoggerFactory.getLogger(CoppClarkHolidayAndExchangeTransformer::class.java)
    }

    /**
     * Again, note we use `out TableEntity` to match the StreamOperator generics
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun process(inputFlow: Flow<CsvRow<Map<String, String>>>): Flow<DbOperation<out TableEntity>> {
        val dbHolidays = entityDb.getRange(ExchangeHoliday.ByHolidayTypeIsoMicHolidayDate(holidayType))
            .toList()
            .associateBy { getHolidayMapKey(it.isoMic, it.holidayType, it.holidayDate) }
            .toMutableMap()

        //Additionally load all the exchange records in the system mapped on the key - isoMic
        val dbExchanges = entityDb.getBulk<Exchange>().toList().associateBy {
            it.isoMic
        }.toMutableMap()

        //Collect fileExchanges into this map
        val fileExchanges = mutableMapOf<String, Exchange>()

        val fileHolidays = inputFlow
            //Extract the exchange and ensure in map.
            //There are obviously multiple rows with same data but using map ensures we have unique list
            //and there is minimal operational overhead
            .onEach { row ->
                val rowExchange = extractExchange(row)
                if (rowExchange != null) {
                    fileExchanges.putIfAbsent(rowExchange.isoMic, rowExchange)
                }
            }
            .mapNotNull { csvRow ->
                convertToExchangeHoliday(csvRow)
            }
            .toList()
            .associateBy {
                getHolidayMapKey(it.isoMic, it.holidayType, it.holidayDate)
            }
            .toMutableMap()

        //Filter only those we need, see getExchangeRecordsToUpsert for more details
        val exchangesToUpsert = getExchangeRecordsToUpsert(fileExchanges, dbExchanges)

        val fileHolidaysToIgnore = fileHolidays.keys.intersect(dbHolidays.keys)
        val dbHolidaysToRetain = dbHolidays.keys.intersect(fileHolidays.keys)

        fileHolidays.keys.removeAll(fileHolidaysToIgnore)
        dbHolidays.keys.removeAll(dbHolidaysToRetain)

        log.info("Inserting ${fileHolidays.size} exchangeHoliday records")
        log.info("Removing ${dbHolidays.size} exchangeHoliday records")

        val insertSet = fileHolidays.values.map { DbOperation.Insert(it) }.asFlow()
        val deleteSet = dbHolidays.values.map { DbOperation.Delete(it) }.asFlow()

        //Map exchange set left to a set of upserts. Audit will still be clean as exchangesToUpsert only includes new/modified
        val exchangeSet = exchangesToUpsert.map { DbOperation.Upsert(it) }.asFlow()
        log.info("Upserting ${exchangesToUpsert.size} exchange records")

        // Concat all the DB Operations into same flow. We are using `out TableEntity` type so all Db entities are fine
        return flowOf(insertSet, deleteSet, exchangeSet).flattenConcat()
    }

    /**
     * Extract an Exchange DB entity record from a file row
     */
    private fun extractExchange(row: CsvRow<Map<String, String>>): Exchange? {
        val mic = row.data["ISO MIC Code"]
        val country = row.data["ISOCountryCode"]
        val exchangeName = row.data["ExchangeName"]

        return if (mic != null && country != null && exchangeName != null) {
            Exchange {
                isoCountry = country
                isoMic = mic
                name = exchangeName
            }
        } else {
            null
        }
    }

    /**
     * Compare file exchanges vs those in the DB.
     * Exchanges are reference data so we don't need to delete any missing in a file.
     * Resulting list should be any which are new (in file, but not DB) or updated
     * in the file compared to the DB
     */
    private fun getExchangeRecordsToUpsert(
        fileExchanges: Map<String, Exchange>,
        dbExchanges: Map<String, Exchange>
    ): List<Exchange> {
        return fileExchanges.filter { fileExchangeMap ->
            val fileExchange = fileExchangeMap.value
            val dbExchange = dbExchanges[fileExchange.isoMic]
            //Log changes
            if (dbExchange == null) {
                log.info("Adding new exchange ${fileExchange.isoMic}")
            } else if (fileExchange.name != dbExchange.name ||
                fileExchange.isoCountry != dbExchange.isoCountry
            ) {
                log.info("Updating exchange ${fileExchange.isoMic}")
            }
            //filter our any that are not new or updated
            dbExchange == null ||
                    fileExchange.name != dbExchange.name ||
                    fileExchange.isoCountry != dbExchange.isoCountry
        }.map { it.value }
    }
}
