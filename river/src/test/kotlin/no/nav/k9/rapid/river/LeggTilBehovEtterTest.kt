package no.nav.k9.rapid.river

import de.huxhorn.sulky.ulid.ULID
import no.nav.k9.rapid.behov.Behov
import no.nav.k9.rapid.behov.Behovssekvens
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

internal class LeggTilBehovEtterTest {

    @Test
    fun `Legge til behov etter første behov`() {
        val jsonMessage = behovssekvens.somJsonMessage()
        jsonMessage.leggTilLøsning("Foo")
        jsonMessage.leggTilBehovEtter("Foo", Behov(navn = "1"), Behov(navn = "2"))
        jsonMessage.assert(listOf("Foo", "1", "2", "Bar", "Car"))
    }

    @Test
    fun `Legge til behov etter siste behov`() {
        val jsonMessage = behovssekvens.somJsonMessage()
        jsonMessage.leggTilLøsning("Car")
        jsonMessage.leggTilBehovEtter("Car", Behov(navn = "3"), Behov(navn = "4"))
        jsonMessage.assert(listOf("Foo", "Bar", "Car", "3", "4"))
    }

    @Test
    fun `Legg til behov i midten`() {
        val jsonMessage= behovssekvens.somJsonMessage()
        jsonMessage.leggTilLøsning("Bar")
        jsonMessage.leggTilBehovEtter("Bar", Behov(navn = "2"))
        jsonMessage.assert(listOf("Foo", "Bar","2", "Car"))
    }

    @Test
    fun `Legge til nye behov uten at aktuelt behov er løst`() {
        val jsonMessage = behovssekvens.somJsonMessage()
        assertThrows<IllegalArgumentException> {
            jsonMessage.leggTilBehovEtter("Foo", Behov(navn = "KanIkkeLeggeTilEtterIkkeLøstBehov"))
        }
    }

    @Test
    fun `Aktuelle behovet finnes ikke`() {
        val jsonMessage = behovssekvens.somJsonMessage()
        assertThrows<IllegalArgumentException> {
            jsonMessage.leggTilBehovEtter("UkjentBehov", Behov(navn = "Foo"))
        }
    }

    @Test
    fun `Nye behovet finnes allerede`() {
        val jsonMessage = behovssekvens.somJsonMessage()
        jsonMessage.leggTilLøsning("Foo")
        assertThrows<IllegalArgumentException> {
            jsonMessage.leggTilBehovEtter("Foo", Behov(navn = "Bar"))
        }
    }

    @Test
    fun `Aktuelle behovet er det eneste`() {
        val jsonMessage = Behovssekvens(
            id = ULID().nextULID(),
            correlationId = UUID.randomUUID().toString(),
            behov = arrayOf(
                Behov(navn = "Foo")
            )
        ).somJsonMessage()
        jsonMessage.leggTilLøsning("Foo")
        jsonMessage.leggTilBehovEtter("Foo", Behov(navn = "Bar"), Behov(navn = "Car"))
        jsonMessage.assert(listOf("Foo", "Bar", "Car"))
    }

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
}