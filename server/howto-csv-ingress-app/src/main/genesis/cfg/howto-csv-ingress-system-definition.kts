/**
 * System              : Genesis Business Library
 * Sub-System          : multi-pro-code-test Configuration
 * Version             : 1.0
 * Copyright           : (c) Genesis
 * Date                : 2022-03-18
 * Function : Provide system definition config for multi-pro-code-test.
 *
 * Modification History
 */
systemDefinition {
    global {
        //This defines the system definitions needed by this project
        //They tell the respective pipeline where to retrieve the files to load
        //They must be overridden as needed for the environments a project is
        //in genesis-system-definition.kts.
        item(name = "CCSimpleExample", value = "OVERRIDE_IN_APP_ENV")
        item(name = "CCSnapshotExample", value = "OVERRIDE_IN_APP_ENV")
        item(name = "CCSnapshotExchangeExample", value = "OVERRIDE_IN_APP_ENV")
    }

    systems {

    }

}