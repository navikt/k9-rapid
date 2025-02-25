package no.nav.k9.rapid.river

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.isMissingOrNull
import de.huxhorn.sulky.ulid.ULID
import no.nav.k9.rapid.behov.Behov
import no.nav.k9.rapid.behov.Behovssekvens
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

internal class LeggTilLøsningTest {

    private companion object {
        val behovssekvens = Behovssekvens(
            id = ULID().nextULID(),
            correlationId = UUID.randomUUID().toString(),
            behov = arrayOf(
                Behov(navn = "Foo"),
                Behov(navn = "Bar")
            )
        )
    }

    @Test
    fun `Legge til løsninger`() {
        val jsonMessage = behovssekvens.somJsonMessage()
        jsonMessage.assertInneholderIngenLøsninger()

        jsonMessage.leggTilLøsning("Foo", mapOf("løsning" to true))
        jsonMessage.assertInneholderLøsninger("Foo")

        jsonMessage.leggTilLøsning("Bar", mapOf("løsning" to true))
        jsonMessage.assertInneholderLøsninger("Foo", "Bar")
    }
}

private fun JsonMessage.assertInneholderIngenLøsninger() = assertTrue(get(Løsninger).isMissingOrNull())

private fun JsonMessage.assertInneholderLøsninger(vararg forventetLøsninger: String) {
    val løsninger = (get(Løsninger) as ObjectNode).fieldNames().asSequence().toList()
    assertEquals(forventetLøsninger.size, løsninger.size)
    assertTrue(løsninger.containsAll(løsninger))
}