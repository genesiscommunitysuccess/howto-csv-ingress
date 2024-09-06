plugins {
    kotlin("plugin.power-assert")
    id("global.genesis.bdd_automation") version "3.5.76"
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
    implementation("global.genesis:genesis-notify-pipelines:${findProperty("notifyVersion")}")
    testImplementation("io.rest-assured:rest-assured:4.4.0")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("com.sun.xml.bind:jaxb-core:2.3.0")
    implementation("com.sun.xml.bind:jaxb-impl:2.3.0")
    testImplementation("io.rest-assured:json-schema-validator:6.3.0")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    implementation("com.hierynomus:sshj:0.38.0")
    implementation("software.amazon.awssdk:s3:2.20.0")
    implementation("com.github.docker-java:docker-java:3.4.0")
    implementation("com.github.docker-java:docker-java-transport-httpclient5:3.4.0")
    // docker-java's default Jersey transport needs javax.annotation
    testImplementation("javax.annotation:javax.annotation-api:1.3.2")

}

description = "howto-csv-ingress-app"

sourceSets {
    main {
        resources {
            srcDirs("src/main/resources", "src/main/genesis")
        }
    }
}

// Genesis needs Groovy 3 and the BDD library Groovy 4, so the in-JVM Genesis tests run in their own suite
val genesisTest = sourceSets.create("genesisTest") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
    resources.srcDir("src/test/resources")
}

configurations {
    named(genesisTest.implementationConfigurationName) { extendsFrom(testImplementation.get()) }
    named(genesisTest.runtimeOnlyConfigurationName) { extendsFrom(testRuntimeOnly.get()) }
    named(genesisTest.compileClasspathConfigurationName) { exclude(group = "org.apache.groovy") }
    named(genesisTest.runtimeClasspathConfigurationName) { exclude(group = "org.apache.groovy") }
    testCompileClasspath { exclude(group = "org.codehaus.groovy") }
    testRuntimeClasspath { exclude(group = "org.codehaus.groovy") }
}

val genesisTestTask = tasks.register<Test>("genesisTest") {
    description = "Runs the in-JVM Genesis tests."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    testClassesDirs = genesisTest.output.classesDirs
    classpath = genesisTest.runtimeClasspath
    shouldRunAfter(tasks.test)
    // Genesis sets its test JVM flags and system properties on test only
    val unitTest = tasks.test.get()
    jvmArgs(unitTest.jvmArgs)
    systemProperties(unitTest.systemProperties)
}

tasks.check { dependsOn(genesisTestTask) }
