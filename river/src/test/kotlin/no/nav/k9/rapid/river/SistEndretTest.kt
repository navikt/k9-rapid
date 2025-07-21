package no.nav.k9.rapid.river

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.isMissingOrNull
import com.github.navikt.tbd_libs.rapids_and_rivers_api.FailedMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.OutgoingMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.SentMessage
import de.huxhorn.sulky.ulid.ULID
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.k9.rapid.behov.Behov
import no.nav.k9.rapid.behov.Behovssekvens
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep
import java.time.ZonedDateTime
import java.util.*

internal class SistEndretTest {
    @Test
    fun `Sist endret blir oppdatert i JsonMessage`() {
        val jsonMessage = JsonMessage.newMessage(mapOf()).also { it.interestedIn("@sistEndret") }
        assertTrue(jsonMessage["@sistEndret"].isMissingOrNull())

        jsonMessage.oppdaterSistEndret()

        assertNotNull(jsonMessage["@sistEndret"].asZonedDateTime())
    }

    @Test
    fun `Sist endret blir oppdatert ved bruk av sendMedId`() {
        val jsonMessage = Behovssekvens(
            id = ULID().nextULID(),
            correlationId = UUID.randomUUID().toString(),
            behov = arrayOf(
                Behov(navn = "Foo")
            )
        ).somJsonMessage()

        val opprettet = jsonMessage["@behovOpprettet"].asZonedDateTime()
        val sistEndret = jsonMessage["@sistEndret"].asZonedDateTime()
        assertNotNull(opprettet)
        assertEquals(opprettet, sistEndret)

        val messageContext = object: MessageContext {
            var message: String? = null
            override fun publish(message: String) = throw IllegalStateException("Ikke brukt.")
            override fun publish(messages: List<OutgoingMessage>) = throw IllegalStateException("Ikke brukt.")
            override fun publish(key: String, message: String) {
                this.message = message
            }

            override fun rapidName(): String {
                return this.toString()
            }

            fun jsonMessage() = JsonMessage(message!!, MessageProblems(message!!)).also { it.interestedIn("@sistEndret") }
        }

        sleep(50)
        messageContext.sendMedId(jsonMessage)
        val oppdatertSistEndret = messageContext.jsonMessage()["@sistEndret"].asZonedDateTime()
        assertNotNull(oppdatertSistEndret)
        assertTrue(oppdatertSistEndret.isAfter(sistEndret))
    }

    private fun JsonNode.asZonedDateTime() = asText().let { ZonedDateTime.parse(it) }
}