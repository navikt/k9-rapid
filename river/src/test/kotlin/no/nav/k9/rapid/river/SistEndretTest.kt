package no.nav.k9.rapid.river

import com.fasterxml.jackson.databind.JsonNode
import de.huxhorn.sulky.ulid.ULID
import no.nav.helse.rapids_rivers.*
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

        val opprettet = jsonMessage["@opprettet"].asZonedDateTime()
        val sistEndret = jsonMessage["@sistEndret"].asZonedDateTime()
        assertNotNull(opprettet)
        assertEquals(opprettet, sistEndret)

        val messageContext = object: RapidsConnection.MessageContext {
            var message: String? = null
            override fun send(message: String) = throw IllegalStateException("Ikke brukt.")
            override fun send(key: String, message: String) {
                this.message = message
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