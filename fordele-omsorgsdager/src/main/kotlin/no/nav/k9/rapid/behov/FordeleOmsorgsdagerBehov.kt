package no.nav.k9.rapid.behov

import java.time.LocalDate
import java.time.ZonedDateTime

class FordeleOmsorgsdagerBehov(
    val fra: Fra,
    val til: Til,
    val barn: List<Barn> = listOf(),
    val journalpostIder: List<String>,
    val mottatt: ZonedDateTime
) : Behov(
    navn = Navn,
    input = mapOf(
        "versjon" to "1.0.0",
        "fra" to fra,
        "til" to til,
        "barn" to barn,
        "journalpostIder" to journalpostIder,
        "mottatt" to mottatt
    )
) {

    override fun mangler() : List<String> {
        val mangler = mutableListOf<String>()

        if (!fra.identitetsnummer.erGyldigIdentitetsnummer()) {
            mangler.add("identitesnummer på fra er ugylidig")
        }

        if (!til.identitetsnummer.erGyldigIdentitetsnummer()) {
            mangler.add("identitesnummer på til er ugyliddg")
        }

        barn.forEachIndexed { index, b ->
            if (!b.identitetsnummer.erGyldigIdentitetsnummer()) mangler.add("'barn[$index].identitetsnummer er ugyldig (var ${b.identitetsnummer})")
        }

        journalpostIder.forEachIndexed { index, jpid ->
            if (!jpid.erGyldigJournalpostId()) mangler.add("journalpostIder[$index] er ugylig (var $jpid)")
        }

        return mangler
    }

    data class Fra(
        val identitetsnummer: String
    )

    data class Til(
        val identitetsnummer: String
    )

    data class Barn(
        val identitetsnummer: String,
        val fødselsdato: LocalDate
    )

    internal companion object {
        private val journalpostIdRegex = "\\d+".toRegex()
        private val identitetsnummerRegex = "\\d{11}".toRegex()
        private fun String.erGyldigIdentitetsnummer() = this.matches(identitetsnummerRegex)
        private fun String.erGyldigJournalpostId() = this.matches(journalpostIdRegex)
        internal const val Navn = "FordeleOmsorgsdager"
    }
}