package no.nav.k9.rapid.river

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.huxhorn.sulky.ulid.ULID
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.k9.rapid.behov.Behovsformat
import no.nav.k9.rapid.behov.Behovsformat.BehovOpprettet
import no.nav.k9.rapid.behov.Behovsformat.BehovssekvensId
import no.nav.k9.rapid.behov.Behovsformat.Opprettet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

internal class BehovopprettetTest {

    @Test
    fun `henter opprettetBehov før opprettet`() {
        val dato = Behovsformat.nå().toString()
        val annetDato = Behovsformat.nå().minusHours(1).toString()
        assertEquals(dato, mapOf(
                "@opprettet" to annetDato,
                BehovOpprettet to dato,
                BehovssekvensId to "${ULID().nextULID()}"
        ).hentKey(BehovOpprettet))
    }

    @Test
    fun `henter opprettet om behovopprettet ikke finnes`() {
        val dato = Behovsformat.nå().toString()
        assertEquals(dato, mapOf(
            "@opprettet" to dato,
            BehovssekvensId to "${ULID().nextULID()}"
        ).hentKey(Opprettet))
    }

    @Test
    fun `henter behovopprettet om opprettet ikke finnes`() {
        val dato = Behovsformat.nå().toString()
        assertEquals(dato, mapOf(
            BehovOpprettet to dato,
            BehovssekvensId to "${ULID().nextULID()}"
        ).hentKey(BehovOpprettet))
    }

    private companion object {
        private fun Map<String, String>.hentKey(key: String) = jacksonObjectMapper().writeValueAsString(this).let { json ->
            JsonMessage(
                originalMessage = json,
                problems = MessageProblems(originalMessage = json)
            ).also { jsonMessage ->
                jsonMessage.interestedIn(BehovssekvensId, "@opprettet", BehovOpprettet)
            }.getString(key)
        }
    }
}