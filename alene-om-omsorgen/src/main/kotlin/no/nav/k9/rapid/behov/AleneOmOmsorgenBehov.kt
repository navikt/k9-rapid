package no.nav.k9.rapid.behov

import java.time.LocalDate

class AleneOmOmsorgenBehov(
    val identitetsnummer: String,
    mottaksdato: LocalDate,
    val barn: List<Barn>
) : Behov(
    navn = Navn,
    input = mapOf(
        "versjon" to "1.0.0",
        "identitetsnummer" to identitetsnummer,
        "mottaksdato" to "$mottaksdato",
        "barn" to barn.map { b -> mapOf(
            "identitetsnummer" to b.identitetsnummer,
            "fødselsdato" to "${b.fødselsdato}"
        )}.also { require(it.isNotEmpty()) { "Må inneholde minst et barn." }}
    )) {

    override fun mangler() : List<String> {
        val mangler = mutableListOf<String>()

        if (!identitetsnummer.erGyldigIdentitetsnummer()) {
            mangler.add("identitesnummer er ugylidig.")
        }

        barn.forEachIndexed { index, barn ->
            if (!barn.identitetsnummer.erGyldigIdentitetsnummer()) mangler.add("barn[$index].identitetsnummer er ugyldig.")
        }

        return mangler
    }

    data class Barn(
        val identitetsnummer: String,
        val fødselsdato: LocalDate
    )

    internal companion object {
        private val identitetsnummerRegex = "\\d{11}".toRegex()
        private fun String.erGyldigIdentitetsnummer() = this.matches(identitetsnummerRegex)
        internal const val Navn = "AleneOmOmsorgen"
    }
}