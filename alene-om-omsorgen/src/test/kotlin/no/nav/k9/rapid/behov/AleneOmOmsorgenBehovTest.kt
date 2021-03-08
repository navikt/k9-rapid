package no.nav.k9.rapid.behov

import de.huxhorn.sulky.ulid.ULID
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import java.util.*

internal class MidlertidigAleneBehovTest {

    @Test
    fun `Gyldig behov for alene om omsorgen`() {
        behov().gyldigBehovssekvens()
    }

    @Test
    fun `Serialisering av behov for alene om omsorgen`() {
        val mottaksdato = "2020-11-10"

        val forventet = """
            {
                "versjon": "1.0.0",
                "identitetsnummer": "11111111111",
                "mottaksdato": "$mottaksdato",
                "barn": [{
                    "identitetsnummer": "11111111112",
                    "fødselsdato": "2005-05-05"
                }, {
                    "identitetsnummer": "11111111113",
                    "fødselsdato": "2010-01-04"
                }]
            }
        """.trimIndent()

        val behov = behov(
            mottaksdato = LocalDate.parse(mottaksdato)
        )

        JSONAssert.assertEquals(forventet, behov.json.toString(), true)
    }
}

private fun behov(
    mottaksdato: LocalDate = LocalDate.now()
) = AleneOmOmsorgenBehov(
    mottaksdato = mottaksdato,
    identitetsnummer = "11111111111",
    barn = listOf(
        AleneOmOmsorgenBehov.Barn(identitetsnummer = "11111111112", fødselsdato = LocalDate.parse("2005-05-05")),
        AleneOmOmsorgenBehov.Barn(identitetsnummer = "11111111113", fødselsdato = LocalDate.parse("2010-01-04"))
    )
)

private fun AleneOmOmsorgenBehov.gyldigBehovssekvens() = Behovssekvens(
    id = ULID().nextULID(),
    correlationId = UUID.randomUUID().toString(),
    behov = arrayOf(this)
)