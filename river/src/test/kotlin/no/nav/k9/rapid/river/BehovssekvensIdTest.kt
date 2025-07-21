package no.nav.k9.rapid.river

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.k9.rapid.behov.Behovsformat.BehovssekvensId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

internal class BehovssekvensIdTest {

    @Test
    fun `henter behovssekvensId fremfor Id om begge er satt`() {
        assertEquals("01G4BETF1K5P9H48T0D19MEMP5", mapOf(
            "@id" to "01G4BETF1KKSRG72PZD15RZKAM",
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
    fun `kaster feil om kun ID er satt`() {
        assertThrows<IllegalStateException> { mapOf(
            "@id" to "01G4BETF1KKSRG72PZD15RZKAM"
        ).behovssekvensId() }
    }

    @Test
    fun `feiler om behovssekvensId ikke er ULID`() {
        assertThrows<IllegalArgumentException> { mapOf(
            BehovssekvensId to "${UUID.randomUUID()}"
        ).behovssekvensId()}
    }

    @Test
    fun `behovssekvensId ULID, id UUID`() {
        assertEquals("01G4BETF1K5P9H48T0D19MEMP5", mapOf(
            "@id" to "${UUID.randomUUID()}",
            BehovssekvensId to "01G4BETF1K5P9H48T0D19MEMP5"
        ).behovssekvensId())
    }

    @Test
    fun `behovssekvensId UUID, id ULID`() {
        assertThrows<IllegalArgumentException> { mapOf(
            "@id" to "01G4BETF1KKSRG72PZD15RZKAM",
            BehovssekvensId to "${UUID.randomUUID()}"
        ).behovssekvensId()}
    }

    private companion object {
        private fun Map<String, String>.behovssekvensId() = jacksonObjectMapper().writeValueAsString(this).let { json ->
            JsonMessage(
                originalMessage = json,
                problems = MessageProblems(originalMessage = json),
                randomIdGenerator = null
            ).also { jsonMessage ->
                jsonMessage.interestedIn("@id", BehovssekvensId)
            }.behovssekvensId()
        }
    }
}