package no.nav.k9.rapid.behov

import de.huxhorn.sulky.ulid.ULID
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

internal class FordeleOmsorgsdagerBehovTest {

    @Test
    fun `Gyldig behov for fordeling av omsorgsdager`() {
        behov().gyldigBehovssekvens()
    }

    @Test
    fun `Serialisering av behov for fordeling av omsorgsdager`() {
        val mottatt = "2020-11-10T15:00:00Z"

        val forventet = """
            {
                "versjon": "1.0.0",
                "mottatt": "$mottatt",
                "fra": {
                    "identitetsnummer": "11111111111"
                },
                "til": {
                    "identitetsnummer": "11111111112"
                },
                "barn": [{
                    "identitetsnummer": "11111111113",
                    "fødselsdato": "2020-01-03"
                },{
                    "identitetsnummer": "11111111114",
                    "fødselsdato": "2020-01-04"
                }],
                "journalpostIder": [
                    "411111111",
                    "411111112"
                ]
            }
        """.trimIndent()

        val behov = behov(
            mottatt = ZonedDateTime.parse(mottatt)
        )


        JSONAssert.assertEquals(forventet, behov.json.toString(), true)
    }
}

private fun behov(
    mottatt: ZonedDateTime = ZonedDateTime.now()
) = FordeleOmsorgsdagerBehov(
    mottatt = mottatt,
    fra = FordeleOmsorgsdagerBehov.Fra(
        identitetsnummer = "11111111111",
    ),
    til = FordeleOmsorgsdagerBehov.Til(
        identitetsnummer = "11111111112",
    ),
    barn = listOf(
        FordeleOmsorgsdagerBehov.Barn(
            identitetsnummer = "11111111113", fødselsdato = LocalDate.parse("2020-01-03")),
        FordeleOmsorgsdagerBehov.Barn(
            identitetsnummer = "11111111114", fødselsdato = LocalDate.parse("2020-01-04"))
    ),
    journalpostIder = listOf(
        "411111111",
        "411111112"
    )
)

private fun FordeleOmsorgsdagerBehov.gyldigBehovssekvens() = Behovssekvens(
    id = ULID().nextULID(),
    correlationId = UUID.randomUUID().toString(),
    behov = arrayOf(this)
)