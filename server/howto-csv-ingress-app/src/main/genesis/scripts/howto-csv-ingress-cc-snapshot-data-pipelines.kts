package scripts

import global.genesis.db.rx.entity.multi.AsyncEntityDb
import global.genesis.gen.dao.enums.howto_csv_ingress.exchange_holiday.HolidayType
import global.genesis.notify.api.enums.Severity
import global.genesis.pipeline.pal.api.inject
import howto.csv.ingress.copp.clark.transform.CoppClarkHolidayTransformer

val entityDb = inject<AsyncEntityDb>()

pipelines {

    pipeline("CC_SETTLEMENT_SNAPSHOT") {
        source(camelSource { location = getDefaultLocalFileCamelLocation(systemDefinition.get("CCSnapshotExample").get(), "ExchangeSettlement") })
            .map { LOG.info("Triggered CC_SETTLEMENT_SNAPSHOT pipeline"); it}
            .split(csvRawDecoder())
            .transform(CoppClarkHolidayTransformer(entityDb, HolidayType.Settlement))
            //Now using a transactional sink to DB. This will insert all or no changes from a given file
            .sink(txDbSink())
            .onCompletion { context ->
                LOG.info("Completed CC_SETTLEMENT_SNAPSHOT. File: ${context.data.name}")
            }.onCompletion(
                //Create a screen notification for everyone using the app where a file is successfully loaded
                notifyAllScreensOnCompletion {
                    body = "Finished processing filename ${context.data.name}"
                    header = "Finished Processing Copp Clark Exchange Holidays File"
                    severity = Severity.Information
                }
            )
    }

    //Look for trading files separately as they contain similar but separate snapshot data from Copp Clark
    pipeline("CC_TRADING_SNAPSHOT") {
        source(camelSource { location = getDefaultLocalFileCamelLocation(systemDefinition.get("CCSnapshotExample").get(), "ExchangeTrading*.csv") })
            .map { LOG.info("Triggered CC_TRADING_SNAPSHOT pipeline"); it}
            .split(csvRawDecoder())
            .transform(CoppClarkHolidayTransformer(entityDb, HolidayType.Trading))
            .sink(txDbSink())
            .onCompletion { context ->
                LOG.info("Completed CC_TRADING_SNAPSHOT. File: ${context.data.name}")
            }.onCompletion(
                notifyAllScreensOnCompletion {
                    body = "Finished processing filename ${context.data.name}"
                    header = "Finished Processing Copp Clark Exchange Holidays File"
                    severity = Severity.Information
                }
            )
    }

}