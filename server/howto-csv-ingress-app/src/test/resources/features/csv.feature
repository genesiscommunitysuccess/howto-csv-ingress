Feature: CSV File Upload
  I want to upload a CSV file into the system,
  So that I can use the data for analysis.

  @CSV @API
  Scenario Outline: Uploading an invalid file format/malformed form <ExternalLocation>
    Given I connect to "<ExternalLocation>" with a connection "<param>"
    When I copy a CSV file named "<filename>" to "<FileDestination>"
    Then I should see an error message "Invalid file format. Please upload a CSV file."
    Examples:
      | ExternalLocation | param | filename                                                                     | FileDestination                             |
      | Docker           | app   | src/test/resources/5-input/simple/settlement/ExchangeSettlementExample.txt   | /app/run/localDevFileDrop/coppClarkSimple   |
      | Docker           | app   | src/test/resources/5-input/simple/trading/ExchangeTradingExample.txt         | /app/run/localDevFileDrop/coppClarkSimple   |
      | Docker           | app   | src/test/resources/5-input/snapshot/settlement/ExchangeSettlementExample.txt | /app/run/localDevFileDrop/coppClarkSnapshot |
      | Docker           | app   | src/test/resources/5-input/snapshot/trading/ExchangeTradingExample.txt       | /app/run/localDevFileDrop/coppClarkSnapshot |

  @CSV @API
  Scenario Outline: Uploading successful CSV from <ExternalLocation>
    Given I connect to "<ExternalLocation>" with a connection "<param>"
    When I copy a CSV file named "<filename>" to "<FileDestination>"
    Then I should get a success confirmation
    And I should see the file name "<filename>" in the folder "<FolderDone>" within timeout "<timeout>" seconds
    Then I verify that the data from the file "<filename>" is loaded into the database

    Examples:
      | ExternalLocation | param | filename                                                                           | FileDestination                                     | FolderDone                                                | timeout |
      | Docker           | app   | src/test/resources/5-input/simple/settlement/ExchangeSettlementExampleInsert.csv   | /app/run/localDevFileDrop/coppClarkSimple           | /app/run/localDevFileDrop/coppClarkSimple/.done           | 50      |
      | Docker           | app   | src/test/resources/5-input/simple/settlement/ExchangeSettlementExampleUpdate.csv   | /app/run/localDevFileDrop/coppClarkSimple           | /app/run/localDevFileDrop/coppClarkSimple/.done           | 50      |
      | Docker           | app   | src/test/resources/5-input/snapshot/settlement/ExchangeSettlementExampleInsert.csv | /app/run/localDevFileDrop/coppClarkSnapshot         | /app/run/localDevFileDrop/coppClarkSnapshot/.done         | 50      |
      | Docker           | app   | src/test/resources/5-input/snapshot/settlement/ExchangeSettlementExampleRemove.csv | /app/run/localDevFileDrop/coppClarkSnapshot         | /app/run/localDevFileDrop/coppClarkSnapshot/.done         | 50      |
      | Docker           | app   | src/test/resources/5-input/snapshot/trading/ExchangeTradingExampleInsert.csv       | /app/run/localDevFileDrop/coppClarkSnapshotExchange | /app/run/localDevFileDrop/coppClarkSnapshotExchange/.done | 50      |
      | Docker           | app   | src/test/resources/5-input/snapshot/trading/ExchangeTradingExampleUpdate.csv       | /app/run/localDevFileDrop/coppClarkSnapshotExchange | /app/run/localDevFileDrop/coppClarkSnapshotExchange/.done | 50      |
      | Docker           | app   | src/test/resources/5-input/snapshot/trading/ExchangeTradingExampleRemove.csv       | /app/run/localDevFileDrop/coppClarkSnapshotExchange | /app/run/localDevFileDrop/coppClarkSnapshotExchange/.done | 50      |