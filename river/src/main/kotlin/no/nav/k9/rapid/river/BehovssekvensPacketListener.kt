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

    /**
     * returns true: packet sendes
     * returns false/ kaster exception: packet sendes ikke
     */
    abstract fun handlePacket(id: String, packet: JsonMessage) : Boolean

    /**
     * kjøre vurdering om man skal kjøre 'handelePacket' eller ikke.
     * Sjekke om man allerede håndtert melidingen, om ikke steget
     * er idempotent.
     */
    open fun doHandlePacket(id: String, packet: JsonMessage) : Boolean = true

    /**
     * kjøres etter at packet er sendt.
     * Lagre at packet er blitt håndtert
     */
    open fun onSent(id: String, packet: JsonMessage) {}

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        val behovssekvensId = packet.behovssekvensId()
        val correlationId = packet.correlationId()

        withMDC(mapOf(
            BehovssekvensIdKey to behovssekvensId,
            CorrelationIdKey to correlationId)) {

            val doHandlePacket = try { doHandlePacket(
                id = behovssekvensId,
                packet = packet
            )} catch (cause: Throwable) {
                val error = "doHandlePacket kastet exception ${cause::class.simpleName}"
                error.errorSecureLog(packet, cause)
                throw IllegalStateException(error.seSikkerLogg())
            }

            if (!doHandlePacket) {
                return@withMDC
            }

            val handlePacketOk = try {
                handlePacket(id = behovssekvensId, packet = packet).also { if (!it) {
                    "handlePacket returnerte false".also { error ->
                        error.errorApplicationLog()
                        error.errorSecureLog(packet)
                    }
                }}
            } catch (cause: Throwable) {
                "handlePacket kastet exception ${cause::class.simpleName}".also { error ->
                    error.errorApplicationLog()
                    error.errorSecureLog(packet, cause)
                }.let { false }
            }

            if (handlePacketOk) {
                try {
                    context.sendMedId(packet = packet)
                } catch (cause: Throwable) {
                    "sendMedId kastet exception ${cause::class.simpleName}".also { error ->
                        error.errorSecureLog(packet, cause)
                        throw IllegalStateException(error.seSikkerLogg())
                    }
                }

                try {
                    onSent(id = behovssekvensId, packet = packet)
                } catch (cause: Throwable) {
                    "onSent kastet exception ${cause::class.simpleName}".also { error ->
                        error.errorSecureLog(packet, cause)
                        throw IllegalStateException(error.seSikkerLogg())
                    }
                }
            }
        }
    }

    private fun String.seSikkerLogg() = "$this. Se sikker log for mer detaljer."
    private fun String.errorApplicationLog() = logger.error(this.seSikkerLogg())
    private fun String.errorSecureLog(packet: JsonMessage, cause: Throwable? = null) = when (cause) {
        null -> secureLogger.error("$this. ErrorPacket=${packet.toJson()}")
        else -> secureLogger.error("$this. ErrorPacket=${packet.toJson()}", cause)
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

