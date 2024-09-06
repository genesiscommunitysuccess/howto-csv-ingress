package global.genesis

import global.genesis.db.rx.entity.multi.AsyncEntityDb
import global.genesis.db.updatequeue.Bulk
import global.genesis.gen.dao.ExchangeHoliday
import global.genesis.gen.dao.enums.howto_csv_ingress.exchange_holiday.HolidayType
import global.genesis.pipeline.PipelineManager
import global.genesis.testsupport.jupiter.GenesisJunit
import global.genesis.testsupport.jupiter.PackageScan
import global.genesis.testsupport.jupiter.ScriptFile
import global.genesis.testsupport.jupiter.SysDefOverwrite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.awaitility.Durations
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.stream.Stream
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyTo
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.pathString
import kotlin.test.Ignore

// Use Genesis Junit extension
@ExtendWith(GenesisJunit::class)
// When the test process starts it will also start the pipeline from this script
@ScriptFile("howto-csv-ingress-cc-simple-data-pipelines.kts")
@PackageScan(PipelineManager::class)
@SysDefOverwrite("CCSimpleExample", "file://src/test/resources/localDevFileDrop/coppClarkSimple/?")
@Ignore
class SimpleCsvIngressIntegrationTest : CoroutineScope {

    private val dispatcher = Executors.newFixedThreadPool(4).asCoroutineDispatcher()
    override val coroutineContext: CoroutineContext = dispatcher

    // Use real database instance
    @Inject
    lateinit var entityDb: AsyncEntityDb

    private var coppClarkSimplePath: Path = Paths.get("")

    @ParameterizedTest
    @MethodSource("data")
    fun `test simple csv ingress pipeline`(
        inputFilePath: String,
        expectedHolidaysInsertedCount: Int,
        expected2024HolidayNames: Set<String>,
        expected2025HolidayNames: Set<String>,
    ): Unit = runBlocking(coroutineContext) {
        setupFileDrop(filePath = inputFilePath)
        val updates = CopyOnWriteArrayList<ExchangeHoliday>()

        // Listen to updates on the ExchangeHoliday database table
        val subscribeJob = launch {
            entityDb.bulkSubscribe<ExchangeHoliday>()
                .filterIsInstance<Bulk.Update.Insert<ExchangeHoliday>>()
                .onEach { updates.add(it.record) }
                .collect()
        }

        // Verify ExchangeHoliday records that were inserted
        await atMost Durations.ONE_MINUTE until { updates.size == expectedHolidaysInsertedCount }
        val settlementHolidays = updates.filter { it.holidayType == HolidayType.Settlement }
        assert(settlementHolidays.size == expectedHolidaysInsertedCount)
        // Verify same holidays exist for both 2024 and 2025
        val holidayNames2024 = settlementHolidays.getHolidayNamesForYear(2024)
        assert(holidayNames2024 == expected2024HolidayNames)
        val holidayNames2025 = settlementHolidays.getHolidayNamesForYear(2025)
        assert(holidayNames2025 == expected2025HolidayNames)
        subscribeJob.cancel()
    }

    @BeforeEach
    fun setup() {
        // Copy the test files to location where it will be processed by the script file howto-csv-ingress-cc-simple-data-pipelines.kts
        val testResourcesPath = Paths.get("src/test/resources")
        val localDevFileDropPath = testResourcesPath.resolve("localDevFileDrop")
        coppClarkSimplePath = localDevFileDropPath.resolve("coppClarkSimple")
        if (!coppClarkSimplePath.exists()) {
            Files.createDirectories(coppClarkSimplePath)
        }
    }

    private fun setupFileDrop(filePath: String) {
        val testCsvFile = this::class.java.classLoader.getResource(filePath)
        val targetFileCopy = coppClarkSimplePath.resolve("ExchangeSettlementExample.csv")
        if (!targetFileCopy.exists()) {
            Paths.get(testCsvFile.toURI()).copyTo(targetFileCopy)
        }
    }

    @OptIn(ExperimentalPathApi::class)
    @AfterEach
    fun teardown() {
        // Cleaning up test files
        val localDevFileDropPath = coppClarkSimplePath.parent
        if (localDevFileDropPath.exists()) {
            localDevFileDropPath.deleteRecursively()
        }
    }

    private fun List<ExchangeHoliday>.getHolidayNamesForYear(year: Int) =
        filter { it.holidayDate.year == year }.map { it.holidayName }.toSet()

    companion object {
        private val expectedHolidayNames = setOf(
            "Easter Monday",
            "Good Friday",
            "New Year's Day",
            "Early May Bank Holiday",
            "Late May Bank Holiday",
            "Summer Bank Holiday",
            "Christmas",
            "Boxing Day",
        )

        @JvmStatic
        fun data(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    "sampleData/simple/settlement/ExchangeSettlementExample.csv",
                    16,
                    expectedHolidayNames,
                    expectedHolidayNames,
                ),
                Arguments.of(
                    "sampleData/simple-error/settlement/ExchangeSettlementExample.csv",
                    15,
                    expectedHolidayNames - "Good Friday",
                    expectedHolidayNames,
                ),
            )
        }
    }
}
