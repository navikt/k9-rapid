package no.nav.k9.rapid.behov

import java.time.ZonedDateTime

class MidlertidigAleneBehov(
    val søker: Person,
    val annenForelder: Person,
    val journalpostIder: List<String>,
    val mottatt: ZonedDateTime
) : Behov(
    navn = Navn,
    input = mapOf(
        "versjon" to "1.0.0",
        "søker" to mapOf(
            "identitetsnummer" to søker.identitetsnummer
        ),
        "annenForelder" to mapOf(
            "identitetsnummer" to annenForelder.identitetsnummer
        ),
        "journalpostIder" to journalpostIder,
        "mottatt" to mottatt
    )
) {

    override fun mangler() : List<String> {
        val mangler = mutableListOf<String>()

        if (!søker.identitetsnummer.erGyldigIdentitetsnummer()) {
            mangler.add("identitesnummer på søker er ugylidig")
        }

        if (!annenForelder.identitetsnummer.erGyldigIdentitetsnummer()) {
            mangler.add("identitesnummer på annen forelder er ugyliddg")
        }

        journalpostIder.forEachIndexed { index, jpid ->
            if (!jpid.erGyldigJournalpostId()) mangler.add("journalpostIder[$index] er ugylig (var $jpid)")
        }

        return mangler
    }

    data class Person(
        val identitetsnummer: String
    )

    internal companion object {
        private val journalpostIdRegex = "\\d+".toRegex()
        private val identitetsnummerRegex = "\\d{11}".toRegex()
        private fun String.erGyldigIdentitetsnummer() = this.matches(identitetsnummerRegex)
        private fun String.erGyldigJournalpostId() = this.matches(journalpostIdRegex)
        internal const val Navn = "MidlertidigAlene"
    }
}