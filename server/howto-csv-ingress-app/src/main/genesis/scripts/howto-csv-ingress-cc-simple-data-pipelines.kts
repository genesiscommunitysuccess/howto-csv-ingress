package scripts

import global.genesis.pipeline.api.db.DbOperation
import global.genesis.pipeline.file.CsvRow
import howto.csv.ingress.copp.clark.transform.convertToExchangeHoliday

pipelines {

    pipeline("CC_SIMPLE") {
        //Listen to this file location using camel source, see the system definition items for more details
        source(camelSource { location = getDefaultLocalFileCamelLocation(systemDefinition.get("CCSimpleExample").get(),"ExchangeSettlement") })
            //Log that this pipeline has been triggered, map used to log and return the input to next stage of pipeline
            .map { LOG.info("Triggered CC_SIMPLE pipeline"); it}
            //Decode csv file and split into row objects to map
            .split(csvRawDecoder())
            //Map an ExchangeHoliday Entity record from the row data and return a record to Upsert (insert else modify if key exists)
            .map { row ->
                convertToExchangeHoliday(row)?.let { exchangeHoliday -> DbOperation.Upsert(exchangeHoliday) }
            }
            .onOperationError { row: CsvRow<Map<String, String>>, context: PipelineContext<*>, throwable: Throwable ->
                // Error handler if above map operation fails
                // The parameters in this scope are the row that failed, the pipeline context and the error that was thrown
                LOG.warn("Pipeline {} failed to process element {}", context.name, row, throwable)
                // SKIP_ELEMENT will skip the row that failed and continue processing remaining rows
                // Find out more details and alternative options at https://docs.genesis.global/docs/develop/server-capabilities/integrations/data-pipelines/#onoperationerror
                OperationErrorAction.SKIP_ELEMENT
            }
            //Sink mapped items to DB, this is a non transactional db sink, any failures are on the record and
            //others
            .sink(dbSink())
            //Log when we've successfully completed processing a file
            .onCompletion { context ->
                LOG.info("Completed CC_SIMPLE. File: ${context.data.name}")
            }
    }
}
