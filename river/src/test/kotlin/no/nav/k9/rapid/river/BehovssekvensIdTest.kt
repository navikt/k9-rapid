package no.nav.k9.rapid.river

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.k9.rapid.behov.Behovsformat.BehovssekvensId
import no.nav.k9.rapid.behov.Behovsformat.Id
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

internal class BehovssekvensIdTest {

    @Test
    fun `henter behovssekvensId fremfor Id om begge er satt`() {
        assertEquals("01G4BETF1K5P9H48T0D19MEMP5", mapOf(
            Id to "01G4BETF1KKSRG72PZD15RZKAM",
            BehovssekvensId to "01G4BETF1K5P9H48T0D19MEMP5"
        ).behovssekvensId())
    }

    @Test
    fun `henter behovssekvensId om kun den er satt`() {
        assertEquals("01G4BETF1K5P9H48T0D19MEMP5", mapOf(
            BehovssekvensId to "01G4BETF1K5P9H48T0D19MEMP5"
        ).behovssekvensId())
    }

    @Test
    fun `henter Id om kun den er satt`() {
        assertEquals("01G4BETF1KKSRG72PZD15RZKAM", mapOf(
            Id to "01G4BETF1KKSRG72PZD15RZKAM"
        ).behovssekvensId())
    }

    @Test
    fun `feiler om behovssekvensId ikke er ULID`() {
        assertThrows<IllegalArgumentException> { mapOf(
            BehovssekvensId to "${UUID.randomUUID()}",
            Id to "${UUID.randomUUID()}"
        ).behovssekvensId()}

        assertThrows<IllegalArgumentException> { mapOf(
            BehovssekvensId to "${UUID.randomUUID()}",
        ).behovssekvensId()}

        assertThrows<IllegalArgumentException> { mapOf(
            Id to "${UUID.randomUUID()}",
        ).behovssekvensId()}
    }

    @Test
    fun `behovssekvensId ULID, id UUID`() {
        assertEquals("01G4BETF1K5P9H48T0D19MEMP5", mapOf(
            Id to "${UUID.randomUUID()}",
            BehovssekvensId to "01G4BETF1K5P9H48T0D19MEMP5"
        ).behovssekvensId())
    }

    @Test
    fun `behovssekvensId UUID, id ULID`() {
        assertThrows<IllegalArgumentException> { mapOf(
            Id to "01G4BETF1KKSRG72PZD15RZKAM",
            BehovssekvensId to "${UUID.randomUUID()}"
        ).behovssekvensId()}
    }

    private companion object {
        private fun Map<String, String>.behovssekvensId() = jacksonObjectMapper().writeValueAsString(this).let { json ->
            JsonMessage(
                originalMessage = json,
                problems = MessageProblems(originalMessage = json)
            ).also { jsonMessage ->
                jsonMessage.interestedIn(Id, BehovssekvensId)
            }.behovssekvensId()
        }
    }
}