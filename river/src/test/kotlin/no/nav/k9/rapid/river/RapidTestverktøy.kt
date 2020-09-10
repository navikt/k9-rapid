package no.nav.k9.rapid.river

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class VoidMesageContext private constructor(): RapidsConnection.MessageContext {
    override fun send(message: String) {}
    override fun send(key: String, message: String) {}
    internal companion object {
        val Instance = VoidMesageContext()
    }
}

internal class VoidRapidsConnection private constructor(): RapidsConnection() {
    override fun publish(message: String) {}
    override fun publish(key: String, message: String) {}
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

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        sisteMelding = Triple(packet, null, null)
    }
    override fun onSevere(error: MessageProblems.MessageException, context: RapidsConnection.MessageContext) {
        sisteMelding = Triple(null,  error, null)
    }
    override fun onError(problems: MessageProblems, context: RapidsConnection.MessageContext) {
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