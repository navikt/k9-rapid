package no.nav.k9.rapid.river

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.net.InetAddress
import java.util.*

object KafkaBuilder {
    private val log = LoggerFactory.getLogger(KafkaBuilder::class.java)

    fun Environment.kafkaProducer(navn: String): KafkaProducer<String, String> {
        require(navn.isNotBlank()) { "Må sette navn på producer." }
        val baseConfig = BaseProperties.resolve(this)
        val clientId = generateClientId()
        val producerConfig = baseConfig.withProducerConfig(clientId, navn)
        return KafkaProducer(
            producerConfig,
            StringSerializer(),
            StringSerializer()
        )
    }

    private fun Properties.withProducerConfig(clientId: String, navn: String): Properties {
        val producerClientId = "producer-$navn-$clientId"
        log.info("${ProducerConfig.CLIENT_ID_CONFIG} for producer $navn=[$producerClientId]")
        put(ProducerConfig.CLIENT_ID_CONFIG, producerClientId)
        put(ProducerConfig.ACKS_CONFIG, "1")
        put(ProducerConfig.LINGER_MS_CONFIG, "0")
        put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1")
        return this
    }

    private fun Environment.generateClientId(): String {
        if (harEnv("NAIS_APP_NAME")) return InetAddress.getLocalHost().hostName
        return UUID.randomUUID().toString()
    }


    private object BaseProperties {
        fun resolve(environment: Environment) : Properties {
            return when (environment.hentOptionalEnv("KAFKA_PREFER_ON_PREM") == "true") {
                true -> log.info("Henter on prem config ettersom KAFKA_PREFER_ON_PREM er satt til true.").let { onPrem(environment) }
                false -> log.info("Henter aiven config").let { aiven(environment) }
            }
        }

        private fun onPrem(environment: Environment) = Properties().apply {
            val credentials = username()?.let { username ->
                username to password()
            }
            val truststore = environment.hentOptionalEnv("NAV_TRUSTSTORE_PATH")?.let { truststore ->
                truststore to environment.hentRequiredEnv("NAV_TRUSTSTORE_PASSWORD")
            }

            put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, environment.hentRequiredEnv("KAFKA_BOOTSTRAP_SERVERS"))
            put(SaslConfigs.SASL_MECHANISM, "PLAIN")
            put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT")

            credentials?.let { (username, password) ->
                put(
                    SaslConfigs.SASL_JAAS_CONFIG,
                    "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"${username}\" password=\"${password}\";"
                )
            }

            truststore?.let { (path, password) ->
                try {
                    put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL")
                    put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, File(path).absolutePath)
                    put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, password)
                    log.info("Configured '${SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG}' location ")
                } catch (ex: Exception) {
                    log.error("Failed to set '${SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG}' location", ex)
                }
            }
        }

        private fun String.readFile() = try {
            File(this).readText(Charsets.UTF_8)
        } catch (err: FileNotFoundException) {
            null
        }

        private fun username() = "/var/run/secrets/nais.io/service_user/username".readFile()
        private fun password() = requireNotNull("/var/run/secrets/nais.io/service_user/password".readFile()) {
            "Mangler passord på path '/var/run/secrets/nais.io/service_user/password'"
        }

        private fun aiven(environment: Environment) = Properties().apply {
            put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, environment.hentRequiredEnv("KAFKA_BROKERS"))
            put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name)
            put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "")
            put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "jks")
            put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12")
            put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, environment.hentRequiredEnv("KAFKA_TRUSTSTORE_PATH"))
            put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, environment.hentRequiredEnv("KAFKA_CREDSTORE_PASSWORD"))
            put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, environment.hentRequiredEnv("KAFKA_KEYSTORE_PATH"))
            put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, environment.hentRequiredEnv("KAFKA_CREDSTORE_PASSWORD"))
        }
    }
}

