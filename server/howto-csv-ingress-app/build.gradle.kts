plugins {
    kotlin("plugin.power-assert") version "2.1.0"
    id("global.genesis.allure") version "1.0.0"
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
    testImplementation("io.rest-assured:rest-assured:4.4.0")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("com.sun.xml.bind:jaxb-core:2.3.0")
    implementation("com.sun.xml.bind:jaxb-impl:2.3.0")
    testImplementation("io.rest-assured:json-schema-validator:latest.release")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    implementation("com.hierynomus:sshj:0.38.0")
    implementation("software.amazon.awssdk:s3:2.20.0")
    implementation("com.github.docker-java:docker-java:3.4.0")
    implementation("com.github.docker-java:docker-java-transport-httpclient5:3.4.0")

}

description = "howto-csv-ingress-app"

sourceSets {
    main {
        resources {
            srcDirs("src/main/resources", "src/main/genesis")
        }
    }
}
