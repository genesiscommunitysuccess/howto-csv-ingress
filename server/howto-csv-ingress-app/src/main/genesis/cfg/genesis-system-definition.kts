systemDefinition {
    global {
        item(name = "DbLayer", value = "SQL")
        item(name = "SqlEnableSequenceGeneration", value = true)
        item(name = "DictionarySource", value = "DB")
        item(name = "DbHost", value = "jdbc:h2:file:~/genesis-local-db/howto-csv-ingress/h2/test;DB_CLOSE_DELAY=-1;NON_KEYWORDS=VALUE,KEY;AUTO_SERVER=TRUE")
        item(name = "DbQuotedIdentifiers", value = true)
        item(name = "DEPLOYED_PRODUCT", value = "howto-csv-ingress")
        item(name = "MqLayer", value = "ZeroMQ")
        item(name = "AliasSource", value = "DB")
        item(name = "HookStateStore", value = "DB")
        item(name = "MetricsEnabled", value = "false")
        item(name = "ZeroMQProxyInboundPort", value = "5001")
        item(name = "ZeroMQProxyOutboundPort", value = "5000")
        item(name = "DbUsername", value = "Enter DB Username")
        item(name = "DbPassword", value = "Enter DB Password")
        item(name = "DbSqlConnectionPoolSize", value = "3")
        item(name = "DbMode", value = "VANILLA")
        item(name = "GenesisNetProtocol", value = "V2")
        item(name = "ResourcePollerTimeout", value = "5")
        item(name = "ReqRepTimeout", value = "60")
        item(name = "MetadataChronicleMapAverageKeySizeBytes", value = "128")
        item(name = "MetadataChronicleMapAverageValueSizeBytes", value = "1024")
        item(name = "MetadataChronicleMapEntriesCount", value = "512")
        item(name = "DaemonServerPort", value = "4568")
        item(name = "DaemonHealthPort", value = "4569")
        item(
            name = "JVM_OPTIONS",
            value = "-XX:MaxHeapFreeRatio=70 -XX:MinHeapFreeRatio=30 -XX:+UseG1GC -XX:+UseStringDeduplication -XX:OnOutOfMemoryError=\"handleOutOfMemoryError.sh %p\""
        )
    }

    systems {

        system(name = "DEV") {

            hosts {
                host(LOCAL_HOST)
            }
            
            item(name = "ZeroMQConnectToLocalhostViaLoopback", value = "true")
            item(name = "DbNamespace", value = "csvingress")
            item(name = "PrimaryIfSingleNode", value = "true")
            item(name = "ClusterPort", value = "6000")
            item(name = "location", value = "LO")
            item(name = "LogFramework", value = "LOG4J2")
            item(name = "LogFrameworkConfig", value = "log4j2-default.xml")

            /**
             * Here we are setting the file system location for local dev testing
             *
             * For simple local testing we are pointing to the local filesystem, but can easily
             * use camel to point to other locations such as SFTP or S3 bucket
             *
             * In other environments defined in this sysdef we would likely need to override these
             * to point to the correct location for that environment setup
             */
            item(name = "CCSimpleExample", value = "file://localDevFileDrop/coppClarkSimple/?")
            item(name = "CCSnapshotExample", value = "file://localDevFileDrop/coppClarkSnapshot/?")
            item(name = "CCSnapshotExchangeExample", value = "file://localDevFileDrop/coppClarkSnapshotExchange/?")
        }

        system(name = "UAT") {

            hosts {
                host(LOCAL_HOST)
            }

            item(name = "ZeroMQConnectToLocalhostViaLoopback", value = "true")
            item(name = "DbNamespace", value = "howto-csv-ingress")
            item(name = "PrimaryIfSingleNode", value = "true")
            item(name = "ClusterPort", value = "6000")
            item(name = "location", value = "LO")
            item(name = "LogFramework", value = "LOG4J2")
            item(name = "LogFrameworkConfig", value = "log4j2-default.xml")


            /**
             * Here we are setting SFTP endpoints to be used in the UAT environment
             * These endpoints are reliant on the necessary environment variables being set: SFTP_HOST, SFTP_PORT, SFTP_USER and SFTP_PASS
             */
            item(name = "CCSimpleExample", value = "sftp:${env["SFTP_HOST"]}:${env["SFTP_PORT"]}/coppClarkSimple?username=${env["SFTP_USER"]}&password=${env["SFTP_PASS"]}&idempotent=true&noop=true")
            item(name = "CCSnapshotExample", value = "sftp:${env["SFTP_HOST"]}:${env["SFTP_PORT"]}/coppClarkSnapshot?username=${env["SFTP_USER"]}&password=${env["SFTP_PASS"]}&idempotent=true&noop=true")
            item(name = "CCSnapshotExchangeExample", value = "sftp:${env["SFTP_HOST"]}:${env["SFTP_PORT"]}/coppClarkSnapshotExchange?username=${env["SFTP_USER"]}&password=${env["SFTP_PASS"]}&idempotent=true&noop=true")

            /**
             * Here we are setting AWS S3 endpoints to be used in the UAT environment
             * These endpoints are reliant on the necessary environment variables being set: S3_ACCESS_KEY and S3_SECRET_KEY
             */
            // item(name = "CCSimpleExample", value = "aws2-s3://coppClark/coppClarkSimple?accessKey=${env["S3_ACCESS_KEY"]}&secretKey=${env["S3_SECRET_KEY"]}")
            // item(name = "CCSnapshotExample", value = "aws2-s3://coppClark/coppClarkSnapshot?accessKey=${env["S3_ACCESS_KEY"]}&secretKey=${env["S3_SECRET_KEY"]}")
            // item(name = "CCSnapshotExchangeExample", value = "aws2-s3://coppClark/coppClarkSnapshotExchange?accessKey=${env["S3_ACCESS_KEY"]}&secretKey=${env["S3_SECRET_KEY"]}")
        }

    }

}
