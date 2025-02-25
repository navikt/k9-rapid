package no.nav.k9.rapid.river

import com.github.navikt.tbd_libs.rapids_and_rivers.River
import de.huxhorn.sulky.ulid.ULID
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.k9.rapid.behov.Behov
import no.nav.k9.rapid.behov.Behovssekvens
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

internal class LøsningPåTest {

    private companion object {
        val sisteUtfallPacketListener = SisteUtfallPacketListener()
        val behovssekvens = Behovssekvens(
            id = ULID().nextULID(),
            correlationId = UUID.randomUUID().toString(),
            behov = arrayOf(
                Behov(navn = "Foo"),
                Behov(navn = "Bar"),
                Behov(navn = "Car"),
                Behov(navn = "Tzar")
            )
        )

        val harLøsningPåBehovRiver = River(VoidRapidsConnection.Instance).apply {
            register(sisteUtfallPacketListener)
            validate { it.harLøsningPåBehov("Foo", "Bar") }
        }

        val utenLøsningPåBehovRiver = River(VoidRapidsConnection.Instance).apply {
            register(sisteUtfallPacketListener)
            validate { it.utenLøsningPåBehov("Foo", "Bar") }
        }
    }

    @Test
    fun `uten løsninger på noen behov`() {
        val message = behovssekvens.somJsonMessage().toJson()
        harLøsningPåBehovRiver.onMessage(message, VoidMesageContext.Instance, SimpleMeterRegistry())
        var utfall = sisteUtfallPacketListener.sistUtfall().first
        assertEquals(SisteUtfallPacketListener.Utfall.Error, utfall)

        utenLøsningPåBehovRiver.onMessage(message, VoidMesageContext.Instance, SimpleMeterRegistry())
        utfall = sisteUtfallPacketListener.sistUtfall().first
        assertEquals(SisteUtfallPacketListener.Utfall.Packet, utfall)
    }

    @Test
    fun `løsning på ett behov`() {
        val message = behovssekvens.somJsonMessage().leggTilLøsning("Foo").toJson()
        harLøsningPåBehovRiver.onMessage(message, VoidMesageContext.Instance, SimpleMeterRegistry())
        var utfall = sisteUtfallPacketListener.sistUtfall().first
        assertEquals(SisteUtfallPacketListener.Utfall.Error, utfall)

        utenLøsningPåBehovRiver.onMessage(message, VoidMesageContext.Instance, SimpleMeterRegistry())
        utfall = sisteUtfallPacketListener.sistUtfall().first
        assertEquals(SisteUtfallPacketListener.Utfall.Error, utfall)
    }

    @Test
    fun `løsning på begge behov`() {
        val message = behovssekvens.somJsonMessage().leggTilLøsning("Foo").leggTilLøsning("Bar").toJson()
        harLøsningPåBehovRiver.onMessage(message, VoidMesageContext.Instance, SimpleMeterRegistry())
        var utfall = sisteUtfallPacketListener.sistUtfall().first
        assertEquals(SisteUtfallPacketListener.Utfall.Packet, utfall)

        utenLøsningPåBehovRiver.onMessage(message, VoidMesageContext.Instance, SimpleMeterRegistry())
        utfall = sisteUtfallPacketListener.sistUtfall().first
        assertEquals(SisteUtfallPacketListener.Utfall.Error, utfall)
    }
}