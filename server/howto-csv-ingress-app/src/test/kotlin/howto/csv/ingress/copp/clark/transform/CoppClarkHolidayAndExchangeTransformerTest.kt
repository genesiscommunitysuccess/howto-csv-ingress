package howto.csv.ingress.copp.clark.transform

import global.genesis.pipeline.api.db.DbOperation
import global.genesis.db.rx.entity.multi.AsyncEntityDb
import global.genesis.gen.dao.Exchange
import global.genesis.gen.dao.ExchangeHoliday
import global.genesis.gen.dao.enums.howto_csv_ingress.exchange_holiday.HolidayType
import global.genesis.pipeline.file.CsvRow
import global.genesis.testsupport.jupiter.GenesisJunit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import javax.inject.Inject

// Use Genesis Junit extension
@ExtendWith(GenesisJunit::class)
class CoppClarkHolidayAndExchangeTransformerTest {

    // Use real database instance
    @Inject
    lateinit var entityDb: AsyncEntityDb

    @BeforeEach
    fun setup(): Unit = runBlocking {
        // Insert Exchange Holiday into the database which we expect to be deleted as it isn't present in the input flow
        // (which represents the rows from a CSV file)
        entityDb.insert(expectedDeletedHoliday)
        // Insert an existing exchange into the database. This won't match the exchange from the input flow, so will be ignored
        // by the transformer
        entityDb.insert(existingExchange)
    }

    @Test
    fun `test transformer`(): Unit = runBlocking {
        // Create the transformer
        val transformer = CoppClarkHolidayAndExchangeTransformer(
            entityDb = entityDb,
            holidayType = HolidayType.Settlement,
        )
        // Exchange Holiday we expect to be inserted into the database
        val expectedInsertedHoliday = ExchangeHoliday(
            holidayName = "New Year's Day",
            holidayType = HolidayType.Settlement,
            isoMic = "XLON",
            holidayDate = DateTime.parse("2024-01-01T00:00:00.000Z"),
        )
        // Exchange we expect to be inserted into the database
        val expectedInsertedExchange = Exchange(
            name = "London Stock Exchange",
            isoMic = "XLON",
            isoCountry = "GB",
        )
        // Flow representing the rows from a CSV file
        val csvRowInput: Flow<CsvRow<Map<String, String>>> = flow {
            emit(
                CsvRow(
                    1, mapOf(
                        "CenterID" to "818",
                        "ISO MIC Code" to "XLON",
                        "ISOCountryCode" to "GB",
                        "ExchangeName" to "London Stock Exchange",
                        "EventYear" to "2024",
                        "EventDate" to "20240101",
                        "EventDayOfWeek" to "Mon",
                        "EventName" to "New Year's Day",
                        "FileType" to "S",
                    )
                )
            )
        }

        // Process the input with the transformer
        val output = transformer.process(csvRowInput).toList()

        // Check that we have one insert db operation and one delete db operation for Exchange Holidays
        // and one upsert db operation for Exchange
        assert(output.size == 3)
        val insertHolidayOperations = output.filterIsInstance<DbOperation.Insert<ExchangeHoliday>>()
        assert(insertHolidayOperations.size == 1)
        val deleteHolidayOperations = output.filterIsInstance<DbOperation.Delete<ExchangeHoliday>>()
        assert(deleteHolidayOperations.size == 1)
        val upsertExchangeOperations = output.filterIsInstance<DbOperation.Upsert<Exchange>>()
        assert(upsertExchangeOperations.size == 1)

        // Verify the details of the inserted Exchange Holiday
        val insertHoliday = insertHolidayOperations.first().entity
        assert(insertHoliday.holidayName == expectedInsertedHoliday.holidayName)
        assert(insertHoliday.holidayType == expectedInsertedHoliday.holidayType)
        assert(insertHoliday.holidayDate.compareTo(expectedInsertedHoliday.holidayDate) == 0)
        assert(insertHoliday.isoMic == expectedInsertedHoliday.isoMic)

        // Verify the details of the deleted Exchange Holiday
        val deletedHoliday = deleteHolidayOperations.first().entity
        assert(deletedHoliday.holidayName == expectedDeletedHoliday.holidayName)
        assert(deletedHoliday.holidayType == expectedDeletedHoliday.holidayType)
        assert(deletedHoliday.holidayDate.compareTo(expectedDeletedHoliday.holidayDate) == 0)
        assert(deletedHoliday.isoMic == expectedDeletedHoliday.isoMic)

        // Verify the details of the upserted Exchange
        val upsertExchange = upsertExchangeOperations.first().entity
        assert(upsertExchange.name == expectedInsertedExchange.name)
        assert(upsertExchange.isoCountry == expectedInsertedExchange.isoCountry)
        assert(upsertExchange.isoMic == expectedInsertedExchange.isoMic)
    }

    private val expectedDeletedHoliday = ExchangeHoliday(
        holidayName = "Good Friday",
        holidayType = HolidayType.Settlement,
        isoMic = "XLON",
        holidayDate = DateTime.parse("2024-03-29T00:00:00.000Z"),
    )
    private val existingExchange = Exchange(
        name = "New York Stock Exchange",
        isoMic = "XNYS",
        isoCountry = "US",
    )
}
