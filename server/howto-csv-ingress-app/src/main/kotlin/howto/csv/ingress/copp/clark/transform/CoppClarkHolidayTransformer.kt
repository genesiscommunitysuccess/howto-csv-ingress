package howto.csv.ingress.copp.clark.transform

import global.genesis.db.rx.entity.multi.AsyncEntityDb
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
import kotlinx.coroutines.flow.toList
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This Transformer generates a data pipelines StreamOperator, which in this instance takes in
 * a map of CSVRows (those from our decoded file) and will output a series of DbOperations (e.g. inserts, modifies,
 * deletes, upserts) on the ExchangeHolidays
 */
class CoppClarkHolidayTransformer(
    private val entityDb: AsyncEntityDb,
    private val holidayType: HolidayType,
) : StreamOperator<CsvRow<Map<String, String>>, DbOperation<ExchangeHoliday>> {

    companion object {
        val log: Logger = LoggerFactory.getLogger(CoppClarkHolidayTransformer::class.java)
    }

    /**
     * This is the process function we must override for a StreamOperator, it takes the defined input as a flow,
     * and returns the defined output as a flow
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun process(inputFlow: Flow<CsvRow<Map<String, String>>>): Flow<DbOperation<ExchangeHoliday>> {
        // Load all records for this file's HolidayType into a map
        val dbHolidays = entityDb.getRange(ExchangeHoliday.ByHolidayTypeIsoMicHolidayDate(holidayType))
            .toList()
            // Key on our dupe detection
            .associateBy { getHolidayMapKey(it.isoMic, it.holidayType, it.holidayDate) }
            .toMutableMap()

        //Convert rows to ExchangeHoliday objects and add to map with keys matching dbHolidays
        val fileHolidays = inputFlow
            .mapNotNull { csvRow ->
                convertToExchangeHoliday(csvRow)
            }
            .toList()
            .associateBy {
                getHolidayMapKey(it.isoMic, it.holidayType, it.holidayDate)
            }
            .toMutableMap()

        //Get list of holidays which we can ignore as they are in file + DB
        val fileHolidaysToIgnore = fileHolidays.keys.intersect(dbHolidays.keys)
        //Get list of holidays in the DB we need to keep
        val dbHolidaysToRetain = dbHolidays.keys.intersect(fileHolidays.keys)

        // Remove all file holidays that are already in the db
        fileHolidays.keys.removeAll(fileHolidaysToIgnore)
        // Remove all db holidays that were in the file, leaving only those to delete
        dbHolidays.keys.removeAll(dbHolidaysToRetain)

        // Log a summary of activity on this file load
        log.info("Inserting ${fileHolidays.size} exchangeHoliday records")
        log.info("Removing ${dbHolidays.size} exchangeHoliday records")

        // Generate set of holidays in file but not in DB, to insert
        val insertSet = fileHolidays.values.map { DbOperation.Insert(it) }.asFlow()
        // Generate set of holidays in DB but not in File, to delete
        val deleteSet = dbHolidays.values.map { DbOperation.Delete(it) }.asFlow()

        // Concat the DB Operations into a flow, all DB Operations will be returned to be run by the next part of the pipeline
        return flowOf(insertSet, deleteSet).flattenConcat()
    }
}