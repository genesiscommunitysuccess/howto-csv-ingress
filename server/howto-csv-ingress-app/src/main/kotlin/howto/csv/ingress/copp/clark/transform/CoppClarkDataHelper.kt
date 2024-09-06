package howto.csv.ingress.copp.clark.transform

import global.genesis.gen.dao.ExchangeHoliday
import global.genesis.gen.dao.enums.howto_csv_ingress.exchange_holiday.HolidayType
import global.genesis.pipeline.file.CsvRow
import howto.csv.ingress.copp.clark.transform.CoppClarkHolidayTransformer.Companion.log
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Function to parse the file date format (yyyyMMdd) into our entity date type
 */
fun parseCCDateFormat(dateVal: String?): DateTime? {
    return DateTimeFormat.forPattern("yyyyMMdd").parseDateTime(dateVal)
}

/**
 * Function to parse the FileType value into our entity enum type, also log unexpected values
 */
fun getHolidayType(fileTypeVal: String?): HolidayType? {
    return if (fileTypeVal.isNullOrBlank()) {
        log.error("No FileType present")
        null
    } else if (fileTypeVal == "S") {
        HolidayType.Settlement
    } else if (fileTypeVal == "T") {
        HolidayType.Trading
    } else {
        log.error("FileType $fileTypeVal unknown. Only S (Settlement) and T (Trading) are known")
        null
    }
}

/**
 * Function to generate a single String key, we use this for dupe detection
 */
fun getHolidayMapKey(isoMic: String, holidayType: HolidayType, holidayDate: DateTime): String {
    return "${isoMic}|${holidayType}|${holidayDate.millis}"
}

/**
 * Extract an ExchangeHoliday DB entity record from a file row
 */
fun convertToExchangeHoliday(row: CsvRow<Map<String, String>>): ExchangeHoliday? {
    // Parse out details
    val holType = getHolidayType(row.data["FileType"])
    val holDate = parseCCDateFormat(row.data["EventDate"])
    val mic = row.data["ISO MIC Code"]
    val holName = row.data["EventName"]

    return if (holType != null && holDate != null && mic != null) {
        ExchangeHoliday {
            holidayDate = holDate
            holidayName = holName
            holidayType = holType
            isoMic = mic
        }
    } else {
        // Stop processing row if any of the key fields are not present else invalid
        log.error("Error Line = ${row.data}")
        null
    }
}
