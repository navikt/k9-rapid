package no.nav.k9.rapid

import no.nav.k9.rapid.behov.Behovsformat.nå
import no.nav.k9.rapid.behov.OverføreOmsorgsdagerBehov
import no.nav.k9.rapid.losning.OverføreOmsorgsdagerLøsning
import no.nav.k9.rapid.losning.OverføreOmsorgsdagerLøsningResolver
import no.nav.k9.rapid.losning.somMeldingMedTestLøsninger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class OverføreOmsorgsdagerLøsningResolverTest {
    @Test
    fun `Får løsning om meldingen inneholder løsning`() {
        val behovssekvens = gyldigBehovssekvens()
        val løsning = OverføreOmsorgsdagerLøsning(
            løst = nå(),
            utfall = "Gjennomført",
            begrunnelser = listOf("§9-6"),
            overføringer = mapOf(
                "11111111111" to OverføreOmsorgsdagerLøsning.Overføringer(
                    fått = emptyList(),
                    gitt = listOf(
                        OverføreOmsorgsdagerLøsning.OverføringGitt(
                            til = OverføreOmsorgsdagerLøsning.Person(
                                navn =  "Ola Nordmann",
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
            testLøsninger = mapOf(OverføreOmsorgsdagerBehov.Navn to løsning)
        )

        assertTrue(meldingMedLøsning.harLøsningPå(OverføreOmsorgsdagerLøsningResolver.Instance))
        assertEquals(løsning, meldingMedLøsning.løsningPå(OverføreOmsorgsdagerLøsningResolver.Instance).second)
    }
}