package no.nav.k9.rapid

import de.huxhorn.sulky.ulid.ULID
import no.nav.k9.rapid.behov.Behovssekvens
import no.nav.k9.rapid.behov.OverføreKoronaOmsorgsdagerBehov
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

internal class OverføreKoronaOmsorgsdagerBehovTest {
    @Test
    fun `Gyldig behov for overføring av omsorgsdager`() {
        gyldigBehovssekvens()
    }
}

internal fun gyldigBehovssekvens() = Behovssekvens(
    id = ULID().nextULID(),
    correlationId = UUID.randomUUID().toString(),
    behov = arrayOf(OverføreKoronaOmsorgsdagerBehov(
        mottatt = ZonedDateTime.now(),
        fra = OverføreKoronaOmsorgsdagerBehov.OverførerFra(
            identitetsnummer = "11111111111",
            jobberINorge = true
        ),
        til = OverføreKoronaOmsorgsdagerBehov.OverførerTil(
            identitetsnummer = "11111111112"
        ),
        omsorgsdagerTattUtIÅr = 10,
        omsorgsdagerÅOverføre = 5,
        barn = listOf(OverføreKoronaOmsorgsdagerBehov.Barn(
            identitetsnummer = "11111111113",
            fødselsdato = LocalDate.now(),
            aleneOmOmsorgen = true,
            utvidetRett = false
        )),
        journalpostIder = listOf(),
        periode = OverføreKoronaOmsorgsdagerBehov.Periode(
            fraOgMed = LocalDate.parse("2020-01-01"),
            tilOgMed = LocalDate.parse("2020-01-20")
        )
    ))
)