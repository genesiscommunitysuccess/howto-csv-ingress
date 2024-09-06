package scripts

import global.genesis.db.rx.entity.multi.AsyncEntityDb
import global.genesis.gen.dao.enums.howto_csv_ingress.exchange_holiday.HolidayType
import global.genesis.notify.api.enums.Severity
import global.genesis.pipeline.pal.api.inject
import howto.csv.ingress.copp.clark.transform.CoppClarkHolidayAndExchangeTransformer

val entityDb = inject<AsyncEntityDb>()

pipelines {
    pipeline("CC_SETTLEMENT_SNAPSHOT_WITH_EXCHANGE") {
        source(camelSource { location = getDefaultLocalFileCamelLocation(systemDefinition.get("CCSnapshotExchangeExample").get(), "ExchangeSettlement") })
            .map { LOG.info("Triggered CC_SETTLEMENT_SNAPSHOT_WITH_EXCHANGE pipeline"); it}
            .split(csvRawDecoder())
            //We use a different transformer
            .transform(CoppClarkHolidayAndExchangeTransformer(entityDb, HolidayType.Settlement))
            .sink(txDbSink())
            .onCompletion { context ->
                LOG.info("Completed CC_SETTLEMENT_SNAPSHOT_WITH_EXCHANGE. File: ${context.data.name}")
            }.onCompletion(
                notifyAllScreensOnCompletion {
                    body = "Finished processing filename ${context.data.name}"
                    header = "Finished Processing Copp Clark Exchange Holidays File"
                    severity = Severity.Information
                }
            )
    }

    pipeline("CC_TRADING_SNAPSHOT_WITH_EXCHANGE") {
        source(camelSource { location = getDefaultLocalFileCamelLocation(systemDefinition.get("CCSnapshotExchangeExample").get(), "ExchangeTrading") })
            .map { LOG.info("Triggered CC_TRADING_SNAPSHOT_WITH_EXCHANGE pipeline"); it}
            .split(csvRawDecoder())
            //We use a different transformer
            .transform(CoppClarkHolidayAndExchangeTransformer(entityDb, HolidayType.Trading))
            .sink(txDbSink())
            .onCompletion { context ->
                LOG.info("Completed CC_TRADING_SNAPSHOT_WITH_EXCHANGE. File: ${context.data.name}")
            }.onCompletion(
                notifyAllScreensOnCompletion {
                    body = "Finished processing filename ${context.data.name}"
                    header = "Finished Processing Copp Clark Exchange Holidays File"
                    severity = Severity.Information
                }
            )
    }

}