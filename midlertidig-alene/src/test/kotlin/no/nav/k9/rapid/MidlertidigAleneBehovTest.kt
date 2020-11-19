package no.nav.k9.rapid

import de.huxhorn.sulky.ulid.ULID
import no.nav.k9.rapid.behov.Behovssekvens
import no.nav.k9.rapid.behov.MidlertidigAleneBehov
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.ZonedDateTime
import java.util.*

internal class MidlertidigAleneBehovTest {

    @Test
    fun `Gyldig behov for midlertidig alene`() {
        behov().gyldigBehovssekvens()
    }

    @Test
    fun `Serialisering av behov for midlertidig alene`() {
        val mottatt = "2020-11-10T15:00:00Z"

        val forventet = """
            {
                "versjon": "1.0.0",
                "mottatt": "$mottatt",
                "søker": {
                    "identitetsnummer": "11111111111"
                },
                "annenForelder": {
                    "identitetsnummer": "11111111112"
                },
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
) = MidlertidigAleneBehov(
    mottatt = mottatt,
    søker = MidlertidigAleneBehov.Person(
        identitetsnummer = "11111111111",
    ),
    annenForelder = MidlertidigAleneBehov.Person(
        identitetsnummer = "11111111112",
    ),
    journalpostIder = listOf(
        "411111111",
        "411111112"
    )
)

private fun MidlertidigAleneBehov.gyldigBehovssekvens() = Behovssekvens(
    id = ULID().nextULID(),
    correlationId = UUID.randomUUID().toString(),
    behov = arrayOf(this)
)