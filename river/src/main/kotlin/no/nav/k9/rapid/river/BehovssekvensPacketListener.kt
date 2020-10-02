package no.nav.k9.rapid.river

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC

abstract class BehovssekvensPacketListener(
    protected val logger: Logger) : River.PacketListener {
    protected val secureLogger = LoggerFactory.getLogger("tjenestekall")

    abstract fun handlePacket(packet: JsonMessage) : Boolean

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        val behovssekvensId = packet.behovssekvensId()
        val correlationId = packet.correlationId()

        withMDC(mapOf(
            BehovssekvensIdKey to behovssekvensId,
            CorrelationIdKey to correlationId)) {
            val handlePacketOk = try {
                handlePacket(packet).also { if (!it) {
                    logger.error("handlePacket returnerte false. Se sikker logg for mer detaljer.")
                    secureLogger.info("ErrorPacket: ${packet.toJson()}")
                }}
            } catch (cause: Throwable) {
                logger.error("handlePacket kastet exception ${cause::class.simpleName?:"n/a"}. Se sikker logg for mer detaljer.")
                secureLogger.info("ErrorPacket: ${packet.toJson()}", cause)
                false
            }
            if (handlePacketOk) {
                try {
                    context.sendMedId(packet)
                } catch (cause: Throwable) {
                    secureLogger.info("ErrorPacket: ${packet.toJson()}", cause)
                    throw IllegalStateException("sendMedId kastet exception ${cause::class.simpleName?:"n/a"}. Se sikker logg for mer detaljer.")
                }
            }
        }
    }

    private fun withMDC(context: Map<String, String>, block: () -> Unit) {
        val contextMap = MDC.getCopyOfContextMap() ?: emptyMap()
        try {
            MDC.setContextMap(contextMap + context)
            block()
        } finally {
            MDC.setContextMap(contextMap)
        }
    }

    private companion object {
        private const val BehovssekvensIdKey = "behovssekvens_id"
        private const val CorrelationIdKey = "correlation_id"
    }
 }

