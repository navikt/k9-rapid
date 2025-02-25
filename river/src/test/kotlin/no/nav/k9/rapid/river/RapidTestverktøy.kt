package no.nav.k9.rapid.river

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry

internal class VoidMesageContext private constructor(): MessageContext {
    override fun publish(message: String) {}
    override fun publish(key: String, message: String) {}
    override fun rapidName(): String {
        return this.toString()
    }

    internal companion object {
        val Instance = VoidMesageContext()
    }
}

internal class VoidRapidsConnection private constructor(): RapidsConnection() {
    override fun publish(message: String) {}
    override fun publish(key: String, message: String) {}
    override fun rapidName(): String {
        return this.toString()
    }

    override fun start() {}
    override fun stop() {}
    internal companion object {
        val Instance = VoidRapidsConnection()
    }
}

internal class SisteUtfallPacketListener: River.PacketListener {

    private var sisteMelding = Triple<JsonMessage?, MessageProblems.MessageException?, MessageProblems?>(
            null, null, null
    )

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        sisteMelding = Triple(packet, null, null)
    }
    override fun onSevere(error: MessageProblems.MessageException, context: MessageContext) {
        sisteMelding = Triple(null,  error, null)
    }
    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sisteMelding = Triple(null, null, problems)
    }
    internal fun sistUtfall() : Pair<Utfall, String> = when {
        sisteMelding.first != null -> Pair(Utfall.Packet, sisteMelding.first!!.toJson())
        sisteMelding.second != null -> Pair(Utfall.Severe, sisteMelding.second!!.problems.toString())
        sisteMelding.third != null -> Pair(Utfall.Error,  sisteMelding.third!!.toString())
        else -> throw IllegalStateException("Ingen melding mottatt enda.")
    }

    internal enum class Utfall {
        Packet,
        Severe,
        Error
    }
}