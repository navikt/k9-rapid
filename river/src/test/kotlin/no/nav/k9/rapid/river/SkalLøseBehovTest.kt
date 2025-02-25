package no.nav.k9.rapid.river

import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import de.huxhorn.sulky.ulid.ULID
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.k9.rapid.behov.Behov
import no.nav.k9.rapid.behov.Behovssekvens
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
        river.onMessage("{}", VoidMesageContext.Instance, MessageMetadata("", -1, -1, null, emptyMap()), SimpleMeterRegistry())
        val (sisteUtfall, error) = sisteUtfallPacketListener.sistUtfall()
        assertEquals(SisteUtfallPacketListener.Utfall.Error, sisteUtfall)
        assertTrue(error.contains("@type") && error.contains("@versjon"))
    }

    @Test
    fun `Skal ikke løses enda`() {
        val message = behovssekvens.keyValue.second
        assertEquals("Foo", behovssekvens.somJsonMessage().aktueltBehov())
        river.onMessage(message, VoidMesageContext.Instance, MessageMetadata("", -1, -1, null, emptyMap()), SimpleMeterRegistry())
        val (sisteUtfall, error) = sisteUtfallPacketListener.sistUtfall()
        assertEquals(SisteUtfallPacketListener.Utfall.Error, sisteUtfall)
        assertTrue(error.contains("@løsninger.Foo"))
    }

    @Test
    fun `Skal løses nå`() {
        val message = behovssekvens.somJsonMessage().also {
            it.leggTilLøsning("Foo", mapOf("løsning" to true))
        }.also { assertEquals(TestBehov, it.aktueltBehov()) }.toJson()
        river.onMessage(message, VoidMesageContext.Instance, MessageMetadata("", -1, -1, null, emptyMap()), SimpleMeterRegistry())
        val (sisteUtfall, _) = sisteUtfallPacketListener.sistUtfall()
        assertEquals(SisteUtfallPacketListener.Utfall.Packet, sisteUtfall)
    }

    @Test
    fun `Er allerede løst`() {
        val message = behovssekvens.somJsonMessage().also {
            it.leggTilLøsning(TestBehov, mapOf("løsning" to true))
        }.also { assertEquals("Foo", it.aktueltBehov()) }.toJson()
        river.onMessage(message, VoidMesageContext.Instance, MessageMetadata("", -1, -1, null, emptyMap()), SimpleMeterRegistry())
        val (sisteUtfall, error) = sisteUtfallPacketListener.sistUtfall()
        assertEquals(SisteUtfallPacketListener.Utfall.Error, sisteUtfall)
        assertTrue(error.contains("@løsninger.$TestBehov") && error.contains("Alle matchende behov allerede løst."))
    }

    @Test
    fun `Mangler felter for behovssekvens`() {
        val message = behovssekvens.keyValue.second.replace("@behovsrekkefølge", "@FooBar")
        river.onMessage(message, VoidMesageContext.Instance, MessageMetadata("", -1, -1, null, emptyMap()), SimpleMeterRegistry())
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
        }.also { assertEquals(TestBehovMedSuffix, it.aktueltBehov()) }.toJson()
        river.onMessage(message, VoidMesageContext.Instance, MessageMetadata("", -1, -1, null, emptyMap()), SimpleMeterRegistry())
        val (sisteUtfall, _) = sisteUtfallPacketListener.sistUtfall()
        assertEquals(SisteUtfallPacketListener.Utfall.Packet, sisteUtfall)
    }

    @Test
    fun `Skal løse behov med suffix hvor samme behov finnes allerede`() {
        val TestBehovMedSuffix = "TestBehov@journalføring"
        val sisteUtfallPacketListener = SisteUtfallPacketListener()
        val behovssekvens = Behovssekvens(
            id = ULID().nextULID(),
            correlationId = UUID.randomUUID().toString(),
            behov = arrayOf(
                Behov(navn = "Foo"),
                Behov(navn = TestBehov),
                Behov(navn = TestBehovMedSuffix)
            )
        )

        val river = River(VoidRapidsConnection.Instance).apply {
            register(sisteUtfallPacketListener)
            validate { it.skalLøseBehov(TestBehov) }
        }

        val message = behovssekvens.somJsonMessage().also {
            it.leggTilLøsning("Foo", mapOf("løsning" to true))
            it.leggTilLøsning(TestBehov, mapOf("løsning" to true))
        }.also { assertEquals(TestBehovMedSuffix, it.aktueltBehov()) }.toJson()
        river.onMessage(message, VoidMesageContext.Instance, MessageMetadata("", -1, -1, null, emptyMap()), SimpleMeterRegistry())
        val (sisteUtfall, _) = sisteUtfallPacketListener.sistUtfall()
        assertEquals(SisteUtfallPacketListener.Utfall.Packet, sisteUtfall)
    }

    @Test
    fun `Klar til arkivering`() {
        val message = behovssekvens.somJsonMessage().also {
            it.leggTilLøsning("Foo", mapOf("løsning" to true))
            it.leggTilLøsning(TestBehov, mapOf("løsning" to true))
        }
        assertThrows<IllegalArgumentException> { message.aktueltBehov() }
        assertNull(message.aktueltBehovOrNull())
    }
}