package howto.csv.ingress.copp.clark.transform

import global.genesis.pipeline.api.db.DbOperation
import global.genesis.db.rx.entity.multi.AsyncEntityDb
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
class CoppClarkHolidayTransformerTest {

    // Use real database instance
    @Inject
    lateinit var entityDb: AsyncEntityDb

    @BeforeEach
    fun setup(): Unit = runBlocking {
        // Insert Exchange Holiday into the database which we expect to be deleted as it isn't present in the input flow
        // (which represents the rows from a CSV file)
        entityDb.insert(expectedDeletedHoliday)
    }

    @Test
    fun `test transformer`(): Unit = runBlocking {
        // Create the transformer
        val transformer = CoppClarkHolidayTransformer(
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
        val output: List<DbOperation<ExchangeHoliday>> = transformer.process(csvRowInput).toList()

        // Check that we have one insert db operation and one delete db operation
        assert(output.size == 2)
        val insertOperations = output.filterIsInstance<DbOperation.Insert<ExchangeHoliday>>()
        assert(insertOperations.size == 1)
        val deleteOperations = output.filterIsInstance<DbOperation.Delete<ExchangeHoliday>>()
        assert(deleteOperations.size == 1)

        // Verify the details of the inserted Exchange Holiday
        val insertHoliday = insertOperations.first().entity
        assert(insertHoliday.holidayName == expectedInsertedHoliday.holidayName)
        assert(insertHoliday.holidayType == expectedInsertedHoliday.holidayType)
        assert(insertHoliday.holidayDate.compareTo(expectedInsertedHoliday.holidayDate) == 0)
        assert(insertHoliday.isoMic == expectedInsertedHoliday.isoMic)

        // Verify the details of the deleted Exchange Holiday
        val deletedHoliday = deleteOperations.first().entity
        assert(deletedHoliday.holidayName == expectedDeletedHoliday.holidayName)
        assert(deletedHoliday.holidayType == expectedDeletedHoliday.holidayType)
        assert(deletedHoliday.holidayDate.compareTo(expectedDeletedHoliday.holidayDate) == 0)
        assert(deletedHoliday.isoMic == expectedDeletedHoliday.isoMic)
    }

    private val expectedDeletedHoliday = ExchangeHoliday(
        holidayName = "Good Friday",
        holidayType = HolidayType.Settlement,
        isoMic = "XLON",
        holidayDate = DateTime.parse("2024-03-29T00:00:00.000Z"),
    )
}
