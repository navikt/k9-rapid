package no.nav.k9.rapid.river

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.isMissingOrNull
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC

abstract class BehovssekvensPacketListener(
    protected val logger: Logger,
    private val mdcPaths: Map<String, String> = emptyMap()) : River.PacketListener {
    protected val secureLogger = LoggerFactory.getLogger("tjenestekall")

    /**
     * returns true: packet sendes
     * returns false/ kaster exception: packet sendes ikke
     */
    abstract fun handlePacket(id: String, packet: JsonMessage) : Boolean

    /**
     * kjøre vurdering om man skal kjøre 'handelePacket' eller ikke.
     * Sjekke om man allerede har håndtert meldingen, om ikke steget
     * er idempotent eller om det er en melding som skal settes 'på vent'.
     * return false/ kaster exception: går ikke videre til handlePacket.
     */
    open fun doHandlePacket(id: String, packet: JsonMessage) : Boolean = true

    /**
     * kjøres etter at packet er sendt.
     * Lagre at packet er blitt håndtert
     */
    open fun onSent(id: String, packet: JsonMessage) {}

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        val behovssekvensId = packet.behovssekvensId()
        val correlationId = packet.correlationId()

        packet.interestedIn(*mdcPaths.values.toTypedArray())

        val mdcMap = mdcPaths.mapValues { when (packet[it.value].isMissingOrNull()) {
            true -> null
            false -> packet[it.value].asText()
        }}.filterValues { it != null }.mapValues { it.value!! }
            .plus(BehovssekvensIdKey to behovssekvensId)
            .plus(CorrelationIdKey to correlationId)

        withMDC(mdcMap) {
            val doHandlePacket = try { doHandlePacket(
                id = behovssekvensId,
                packet = packet
            )} catch (cause: Throwable) {
                "doHandlePacket kastet exception ${cause::class.simpleName}".also { error ->
                    error.errorApplicationLog()
                    error.errorSecureLog(packet, cause)
                }.let { false }
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

