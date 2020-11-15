package no.nav.k9.rapid

import de.huxhorn.sulky.ulid.ULID
import no.nav.k9.rapid.behov.Behovssekvens
import no.nav.k9.rapid.behov.OverføreOmsorgsdagerBehov
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

internal class OverføreOmsorgsdagerBehovTest {
    @Test
    fun `Gyldig behov for overføring av omsorgsdager`() {
        gyldigBehovssekvens()
    }
}

internal fun gyldigBehovssekvens() = Behovssekvens(
    id = ULID().nextULID(),
    correlationId = UUID.randomUUID().toString(),
    behov = arrayOf(OverføreOmsorgsdagerBehov(
        mottatt = ZonedDateTime.now(),
        fra = OverføreOmsorgsdagerBehov.OverførerFra(
            identitetsnummer = "11111111111",
            jobberINorge = true
        ),
        til = OverføreOmsorgsdagerBehov.OverførerTil(
            identitetsnummer = "11111111112",
            relasjon = OverføreOmsorgsdagerBehov.Relasjon.NåværendeSamboer,
            harBoddSammenMinstEttÅr = false
        ),
        omsorgsdagerTattUtIÅr = 10,
        omsorgsdagerÅOverføre = 5,
        barn = listOf(OverføreOmsorgsdagerBehov.Barn(
            identitetsnummer = "11111111113",
            fødselsdato = LocalDate.now(),
            aleneOmOmsorgen = true,
            utvidetRett = false
        )),
        kilde = OverføreOmsorgsdagerBehov.Kilde.Brev,
        journalpostIder = listOf()
    ))
)