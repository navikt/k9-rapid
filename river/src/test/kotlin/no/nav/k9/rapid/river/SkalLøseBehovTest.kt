package no.nav.k9.rapid.river

import de.huxhorn.sulky.ulid.ULID
import no.nav.helse.rapids_rivers.River
import no.nav.k9.rapid.behov.Behov
import no.nav.k9.rapid.behov.Behovssekvens
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

internal class SkalLøseBehovTest {

    private companion object {
        val TestBehov = "TestBehov"
        val sisteUtfallPacketListener = SisteUtfallPacketListener()
        val behovssekvens = Behovssekvens(
                id = ULID().nextULID(),
                correlationId = UUID.randomUUID().toString(),
                behov = arrayOf(
                        Behov(navn = "Foo"),
                        Behov(navn = TestBehov)
                )
        )

        val river = River(VoidRapidsConnection.Instance).apply {
            register(sisteUtfallPacketListener)
            validate { it.skalLøseBehov(TestBehov) }
        }
    }

    @Test
    fun `Ikke et sekvensielt behov`() {
        river.onMessage("{}", VoidMesageContext.Instance)
        val (sisteUtfall, error) = sisteUtfallPacketListener.sistUtfall()
        assertEquals(SisteUtfallPacketListener.Utfall.Error, sisteUtfall)
        assertTrue(error.contains("@type") && error.contains("@versjon"))
    }

    @Test
    fun `Skal ikke løses enda`() {
        val message = behovssekvens.keyValue.second
        river.onMessage(message, VoidMesageContext.Instance)
        val (sisteUtfall, error) = sisteUtfallPacketListener.sistUtfall()
        assertEquals(SisteUtfallPacketListener.Utfall.Error, sisteUtfall)
        assertTrue(error.contains("@løsninger.Foo"))
    }

    @Test
    fun `Skal løses nå`() {
        val message = behovssekvens.somJsonMessage().also {
            it.leggTilLøsning("Foo", mapOf("løsning" to true))
        }.toJson()
        river.onMessage(message, VoidMesageContext.Instance)
        val (sisteUtfall, _) = sisteUtfallPacketListener.sistUtfall()
        assertEquals(SisteUtfallPacketListener.Utfall.Packet, sisteUtfall)
    }

    @Test
    fun `Er allerede løst`() {
        val message = behovssekvens.somJsonMessage().also {
            it.leggTilLøsning(TestBehov, mapOf("løsning" to true))
        }.toJson()
        river.onMessage(message, VoidMesageContext.Instance)
        val (sisteUtfall, error) = sisteUtfallPacketListener.sistUtfall()
        assertEquals(SisteUtfallPacketListener.Utfall.Error, sisteUtfall)
        assertTrue(error.contains("@løsninger.$TestBehov") && error.contains("Behov allerede løst."))
    }

    @Test
    fun `Mangler felter for behovssekvens`() {
        val message = behovssekvens.keyValue.second.replace("@behovsrekkefølge", "@FooBar")
        river.onMessage(message, VoidMesageContext.Instance)
        val (sisteUtfall, severe) = sisteUtfallPacketListener.sistUtfall()
        assertEquals(SisteUtfallPacketListener.Utfall.Severe, sisteUtfall)
        assertTrue(severe.contains("@behovsrekkefølge"))
    }

    @Test
    fun `Skal løse behov med suffix`() {
        val TestBehovMedSuffix = "TestBehov@journalføring"
        val sisteUtfallPacketListener = SisteUtfallPacketListener()
        val behovssekvens = Behovssekvens(
            id = ULID().nextULID(),
            correlationId = UUID.randomUUID().toString(),
            behov = arrayOf(
                Behov(navn = "Foo"),
                Behov(navn = TestBehovMedSuffix)
            )
        )

        val river = River(VoidRapidsConnection.Instance).apply {
            register(sisteUtfallPacketListener)
            validate { it.skalLøseBehov(TestBehov) }
        }

        val message = behovssekvens.somJsonMessage().also {
            it.leggTilLøsning("Foo", mapOf("løsning" to true))
        }.toJson()
        river.onMessage(message, VoidMesageContext.Instance)
        val (sisteUtfall, _) = sisteUtfallPacketListener.sistUtfall()
        assertEquals(SisteUtfallPacketListener.Utfall.Packet, sisteUtfall)
    }
}