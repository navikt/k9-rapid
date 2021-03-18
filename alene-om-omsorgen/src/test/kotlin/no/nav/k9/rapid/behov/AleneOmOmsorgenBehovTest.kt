package no.nav.k9.rapid.behov

import de.huxhorn.sulky.ulid.ULID
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import java.util.*

internal class AleneOmOmsorgenBehovTest {

    @Test
    fun `Gyldig behov for alene om omsorgen`() {
        behov().gyldigBehovssekvens()
    }

    @Test
    fun `Serialisering av behov for alene om omsorgen`() {
        val mottaksdato = "2020-11-10"

        val forventet = """
            {
                "versjon": "1.1.0",
                "identitetsnummer": "11111111111",
                "mottaksdato": "$mottaksdato",
                "barn": [{
                    "identitetsnummer": "11111111112"
                }, {
                    "identitetsnummer": "11111111113"
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
        AleneOmOmsorgenBehov.Barn(identitetsnummer = "11111111112"),
        AleneOmOmsorgenBehov.Barn(identitetsnummer = "11111111113")
    )
)

private fun AleneOmOmsorgenBehov.gyldigBehovssekvens() = Behovssekvens(
    id = ULID().nextULID(),
    correlationId = UUID.randomUUID().toString(),
    behov = arrayOf(this)
)