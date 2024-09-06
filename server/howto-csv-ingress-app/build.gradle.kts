plugins {
    id("com.bnorm.power.kotlin-power-assert") version "0.13.0"
}

dependencies {
    compileOnly(genesis("script-dependencies"))
    genesisGeneratedCode(withTestDependency = true)
    //Dependency for Camel SFTP
    implementation("org.apache.camel:camel-ftp:4.7.0")
    testImplementation(genesis("dbtest"))
    testImplementation(genesis("testsupport"))
    testImplementation(genesis("pal-eventhandler"))
    testImplementation(genesis("pal-datapipeline"))
    testImplementation("org.junit.jupiter:junit-jupiter-params")

    //Dependency to use Genesis Notify from Pipelines
    implementation("global.genesis:genesis-notify-pipelines:${properties["notifyVersion"]}")
}

description = "howto-csv-ingress-app"

sourceSets {
    main {
        resources {
            srcDirs("src/main/resources", "src/main/genesis")
        }
    }
}
