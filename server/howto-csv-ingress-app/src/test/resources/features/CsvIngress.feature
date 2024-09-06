Feature: CSV File Upload
  I want to upload a CSV file into the system,
  So that I can use the data for analysis.

  @CSV @API
  Scenario Outline: Uploading an invalid file format/malformed form <ExternalLocation>
    Given User connect to "<ExternalLocation>" with a connection "<param>"
    When User copy a file named "<filename>" to "<FileDestination>"
    Then User should see an error message "Invalid file format. Please upload a CSV file."
    Examples:
      | ExternalLocation | param | filename                              | FileDestination                             |
      | Docker           | app   | ExchangeSettlementExample.txt         | /app/run/localDevFileDrop/coppClarkSimple   |
      | Docker           | app   | ExchangeTradingExample.txt            | /app/run/localDevFileDrop/coppClarkSimple   |
      | Docker           | app   | ExchangeSettlementExampleSnapshot.txt | /app/run/localDevFileDrop/coppClarkSnapshot |
      | Docker           | app   | ExchangeTradingExampleSnapshot.txt    | /app/run/localDevFileDrop/coppClarkSnapshot |

  @CSV @API
  Scenario Outline: Uploading successful CSV from <ExternalLocation>
    Given User connect with username "admin" and password "genesis"
    And User connect to "<ExternalLocation>" with a connection "<param>"
    When User copy a file named "<filename>" to "<FileDestination>"
    Then User should get a success confirmation
    And User should see the file name "<filename>" in the folder "<FolderDone>" within timeout "<timeout>" seconds
    Then User verify data is loaded into the database successfully using "<EndPoint>" with "<searchCriteria>" by comparing to the "<expected_result>" with "<primary_key>" and "<keys_to_ignore>"

    Examples:
      | ExternalLocation | param | EndPoint             | searchCriteria          | filename                                    | FileDestination                                     | FolderDone                                                | timeout | expected_result                                   | primary_key | keys_to_ignore      |
      | Docker           | app   | REQ_EXCHANGE_HOLIDAY | HOLIDAY_TYPE=Settlement | ExchangeSettlementExampleInsert.csv         | /app/run/localDevFileDrop/coppClarkSimple           | /app/run/localDevFileDrop/coppClarkSimple/.done           | 50      | E_simpleExchangeSettlementExampleInsertcsv.json   |             | RECORD_ID,TIMESTAMP |
      | Docker           | app   | REQ_EXCHANGE_HOLIDAY | HOLIDAY_TYPE=Settlement | ExchangeSettlementExampleUpdate.csv         | /app/run/localDevFileDrop/coppClarkSimple           | /app/run/localDevFileDrop/coppClarkSimple/.done           | 50      | E_simpleExchangeSettlementExampleUpdatecsv.json   |             | RECORD_ID,TIMESTAMP |
      | Docker           | app   | REQ_EXCHANGE_HOLIDAY | HOLIDAY_TYPE=Settlement | ExchangeSettlementExampleInsertSnapshot.csv | /app/run/localDevFileDrop/coppClarkSnapshot         | /app/run/localDevFileDrop/coppClarkSnapshot/.done         | 50      | E_snapshotExchangeSettlementExampleInsertcsv.json |             | RECORD_ID,TIMESTAMP |
      | Docker           | app   | REQ_EXCHANGE_HOLIDAY | HOLIDAY_TYPE=Settlement | ExchangeSettlementExampleRemoveSnapshot.csv | /app/run/localDevFileDrop/coppClarkSnapshot         | /app/run/localDevFileDrop/coppClarkSnapshot/.done         | 50      | E_snapshotExchangeSettlementExampleRemovecsv.json |             | RECORD_ID,TIMESTAMP |
      | Docker           | app   | REQ_EXCHANGE_HOLIDAY | HOLIDAY_TYPE=Trading    | ExchangeTradingExampleInsertSnapshot.csv    | /app/run/localDevFileDrop/coppClarkSnapshotExchange | /app/run/localDevFileDrop/coppClarkSnapshotExchange/.done | 50      | E_snapshotExchangeTradingExampleInsertcsv.json    |             | RECORD_ID,TIMESTAMP |
      | Docker           | app   | REQ_EXCHANGE_HOLIDAY | HOLIDAY_TYPE=Trading    | ExchangeTradingExampleUpdateSnapshot.csv    | /app/run/localDevFileDrop/coppClarkSnapshotExchange | /app/run/localDevFileDrop/coppClarkSnapshotExchange/.done | 50      | E_snapshotExchangeTradingExampleUpdatecsv.json    |             | RECORD_ID,TIMESTAMP |
      | Docker           | app   | REQ_EXCHANGE_HOLIDAY | HOLIDAY_TYPE=Trading    | ExchangeTradingExampleRemoveSnapshot.csv    | /app/run/localDevFileDrop/coppClarkSnapshotExchange | /app/run/localDevFileDrop/coppClarkSnapshotExchange/.done | 50      | E_snapshotExchangeTradingExampleRemovecsv.json    |             | RECORD_ID,TIMESTAMP |