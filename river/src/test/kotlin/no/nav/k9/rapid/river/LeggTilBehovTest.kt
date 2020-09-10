package no.nav.k9.rapid.river

import com.fasterxml.jackson.databind.node.JsonNodeType
import com.fasterxml.jackson.databind.node.ObjectNode
import de.huxhorn.sulky.ulid.ULID
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.k9.rapid.behov.Behov
import no.nav.k9.rapid.behov.Behovsformat
import no.nav.k9.rapid.behov.Behovssekvens
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

internal class LeggTilBehovTest {

    private companion object {
        val behovssekvens = Behovssekvens(
                id = ULID().nextULID(),
                correlationId = UUID.randomUUID().toString(),
                behov = arrayOf(
                        Behov(navn = "Foo"),
                        Behov(navn = "Bar"),
                        Behov(navn = "Car")
                )
        )
    }
    @Test
    fun `Legge til behov  før første  behov`() {
        val jsonMessage = behovssekvens.somJsonMessage()
        jsonMessage.leggTilBehov("Foo", Behov(navn = "1"), Behov(navn = "2"))
        jsonMessage.assert(listOf("1","2","Foo", "Bar", "Car"))
    }

    @Test
    fun `Legge til  behov før siste behov`() {
        val jsonMessage = behovssekvens.somJsonMessage()
        jsonMessage.leggTilBehov("Car", Behov(navn = "3"), Behov(navn = "4"))
        jsonMessage.assert(listOf("Foo", "Bar", "3", "4", "Car"))
    }

    @Test
    fun `Legg til behov i midten`() {
        val jsonMessage= behovssekvens.somJsonMessage()
        jsonMessage.leggTilBehov("Bar", Behov(navn = "2"))
        jsonMessage.assert(listOf("Foo", "2", "Bar", "Car"))

    }

    @Test
    fun `Legge til nye behov`() {
        val jsonMessage = behovssekvens.somJsonMessage()
        jsonMessage.leggTilLøsning("Foo", mapOf("Løsning" to true))
        assertThrows<IllegalArgumentException> {
            jsonMessage.leggTilBehov("Foo", Behov(navn = "KanIkkeLeggeTilFørAlleredeLøstBehov"))
        }
    }

    @Test
    fun `Aktuelle behovet finnes ikke`() {
        val jsonMessage = behovssekvens.somJsonMessage()
        assertThrows<IllegalArgumentException> {
            jsonMessage.leggTilBehov("UkjentBehov", Behov(navn = "Foo"))
        }
    }

    @Test
    fun `Nye behovet finnes allerede`() {
        val jsonMessage = behovssekvens.somJsonMessage()
        assertThrows<IllegalArgumentException> {
            jsonMessage.leggTilBehov("Foo", Behov(navn = "Bar"))
        }
    }
}

internal fun Behovssekvens.somJsonMessage() = JsonMessage(
        originalMessage = keyValue.second,
        problems = MessageProblems(originalMessage = keyValue.second)
).also { require(erBehovssekvens(it)) }

private fun JsonMessage.assert(forventetBehovsrekkefølge: List<String>) {
    assertEquals(forventetBehovsrekkefølge, get(Behovsformat.Behovsrekkefølge).map { it.asText() })
    val behov = get(Behovsformat.Behov) as ObjectNode
    forventetBehovsrekkefølge.forEach { assertEquals(JsonNodeType.OBJECT, behov[it].nodeType) }
}