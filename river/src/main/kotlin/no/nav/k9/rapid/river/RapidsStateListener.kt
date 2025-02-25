package no.nav.k9.rapid.river

import com.github.navikt.tbd_libs.rapids_and_rivers.KafkaRapid
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import no.nav.k9.rapid.behov.Behovsformat.iso8601
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

class RapidsStateListener(val onStateChange: (state: RapidsState) -> Unit): RapidsConnection.StatusListener {
    override fun onStartup(rapidsConnection: RapidsConnection) = onStateChange(rapidsConnection.state(RapidsStatus.STARTUP))
    override fun onShutdown(rapidsConnection: RapidsConnection) = onStateChange(rapidsConnection.state(RapidsStatus.SHUTDOWN))
    override fun onNotReady(rapidsConnection: RapidsConnection) = onStateChange(rapidsConnection.state(RapidsStatus.NOT_READY))
    override fun onReady(rapidsConnection: RapidsConnection) = onStateChange(rapidsConnection.state(RapidsStatus.READY))

    private fun RapidsConnection.rapidsConnectionIsReady() = when (this)  {
        is KafkaRapid -> this.isReady()
        else -> true
    }

    private fun RapidsConnection.state(status: RapidsStatus) = RapidsState(
        stateAt = ZonedDateTime.now(),
        isReady = this.rapidsConnectionIsReady(),
        status = status
    )

    data class RapidsState(
        val stateAt: ZonedDateTime,
        val status: RapidsStatus,
        val isReady: Boolean) {
        val asMap = mapOf(
            "stateAt" to stateAt.iso8601(),
            "status" to status.name,
            "isReady" to isReady
        )
        fun isHealthy() = when (status) {
            RapidsStatus.READY -> isReady
            RapidsStatus.SHUTDOWN -> statusVedvartMindreEnn5Minutter()
            RapidsStatus.STARTUP -> statusVedvartMindreEnn5Minutter()
            RapidsStatus.NOT_READY -> false
        }

        private fun statusVedvartMindreEnn5Minutter() =
            ChronoUnit.MINUTES.between(stateAt, ZonedDateTime.now()).absoluteValue < 5L

        companion object {
            fun initialState() = RapidsState(
                stateAt = ZonedDateTime.now(),
                status = RapidsStatus.STARTUP,
                isReady = false
            )
        }
    }

    enum class RapidsStatus {
        STARTUP,
        SHUTDOWN,
        NOT_READY,
        READY
    }
}