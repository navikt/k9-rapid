package no.nav.k9.rapid

import no.nav.k9.rapid.behov.Behovsformat.nå
import no.nav.k9.rapid.behov.OverføreKoronaOmsorgsdagerBehov
import no.nav.k9.rapid.losning.OverføreKoronaOmsorgsdagerLøsning
import no.nav.k9.rapid.losning.OverføreKoronaOmsorgsdagerLøsningResolver
import no.nav.k9.rapid.losning.somMeldingMedTestLøsninger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class OverføreKoronaOmsorgsdagerLøsningResolverTest {
    @Test
    fun `Får løsning om meldingen inneholder løsning`() {
        val behovssekvens = gyldigBehovssekvens()
        val løsning = OverføreKoronaOmsorgsdagerLøsning(
            versjon = "1.0.0",
            løst = nå(),
            utfall = "Gjennomført",
            begrunnelser = listOf("§9-6"),
            overføringer = mapOf(
                "11111111111" to OverføreKoronaOmsorgsdagerLøsning.Overføringer(
                    fått = emptyList(),
                    gitt = listOf(
                        OverføreKoronaOmsorgsdagerLøsning.OverføringGitt(
                            til = OverføreKoronaOmsorgsdagerLøsning.Person(
                                navn = OverføreKoronaOmsorgsdagerLøsning.Navn(
                                    fornavn = "Ola",
                                    mellomnavn = null,
                                    etternavn = "Nordmann"
                                ),
                                fødselsdato = LocalDate.now()
                            ),
                            antallDager = 5,
                            gjelderFraOgMed = LocalDate.now(),
                            gjelderTilOgMed = LocalDate.now().plusDays(1)
                        )
                    )
                )
            )
        )
        val meldingMedLøsning = behovssekvens.keyValue.second.somMeldingMedTestLøsninger(
            testLøsninger = mapOf(OverføreKoronaOmsorgsdagerBehov.Navn to løsning)
        )

        assertTrue(meldingMedLøsning.harLøsningPå(OverføreKoronaOmsorgsdagerLøsningResolver.Instance))
        assertEquals(løsning, meldingMedLøsning.løsningPå(OverføreKoronaOmsorgsdagerLøsningResolver.Instance).second)
    }
}