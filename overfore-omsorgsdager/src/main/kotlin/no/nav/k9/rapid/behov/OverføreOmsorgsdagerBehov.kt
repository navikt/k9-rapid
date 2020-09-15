package no.nav.k9.rapid.behov

import java.time.LocalDate

class OverføreOmsorgsdagerBehov(
        val fra: OverførerFra,
        val til: OverførerTil,
        val omsorgsdagerTattUtIÅr: Int,
        val omsorgsdagerÅOverføre: Int,
        val barn: List<Barn> = listOf(),
        val kilde: Kilde,
        val journalpostIder: List<String>
) : Behov(
        navn = Navn,
        input = mapOf(
                "fra" to fra,
                "til" to til,
                "omsorgsdagerTattUtIÅr" to omsorgsdagerTattUtIÅr,
                "omsorgsdagerÅOverføre" to  omsorgsdagerÅOverføre,
                "barn" to barn,
                "kilde" to kilde.name,
                "journalpostIder" to journalpostIder
        )
) {
    override fun mangler() : List<String> {
        val mangler = mutableListOf<String>()

        if (!fra.identitetsnummer.erGyldigIdentitetsnummer()) mangler.add("'fra.identitetsnummer' er ugyldig (var ${fra.identitetsnummer})")
        if (!fra.borINorge || !fra.jobberINorge) mangler.add("For å overføre dager må man bo og jobbe  i Norge.")

        if (!til.identitetsnummer.erGyldigIdentitetsnummer()) mangler.add("'til.identitetsnummer' er ugyldig (var ${til.identitetsnummer})")
        if (til.relasjon == Relasjon.NåværendeSamboer && til.harBoddSammenMinstEttÅr == null) mangler.add("For overføring til nåværende samboer må man opplyse om man har bodd sammen minst ett år.")

        if (fra.identitetsnummer == til.identitetsnummer) mangler.add("'fra.identitetsnummer' kan ikke være lik 'til.identitetsnummer'")

        if (omsorgsdagerTattUtIÅr !in 0..365) mangler.add("'omsorgsdagerTattUtIÅr' må være melllom 0 og 365 (var $omsorgsdagerTattUtIÅr)")
        if (omsorgsdagerÅOverføre !in 1..365) mangler.add("'omsorgsdagerÅOverføre' må være mellom 1 og 365 var($omsorgsdagerÅOverføre)")

        barn.forEachIndexed { index, b ->
            if (!b.identitetsnummer.erGyldigIdentitetsnummer()) mangler.add("'barn[$index].identitetsnummer er ugyldig (var ${b.identitetsnummer})")
        }
        journalpostIder.forEachIndexed { index, jpid ->
            if (!jpid.erGyldigJournalpostId()) mangler.add("'journalpostIder[$index] er ugylig (var $jpid)'")
        }

        return mangler
    }

    enum class Kilde {
        Brev,
        Digital
    }

    enum class Relasjon {
        NåværendeEktefelle,
        NåværendeSamboer
    }

    data class OverførerFra(
            val identitetsnummer: String,
            val jobberINorge: Boolean,
            val borINorge: Boolean
    )

    data class OverførerTil(
            val identitetsnummer: String,
            val relasjon: Relasjon,
            val harBoddSammenMinstEttÅr: Boolean? = null
    )

    data class Barn(
            val identitetsnummer: String,
            val fødselsdato: LocalDate,
            val aleneOmOmsorgen: Boolean,
            val utvidetRett: Boolean
    )

    internal companion object {
        private val journalpostIdRegex = "\\d+".toRegex()
        private val identitetsnummerRegex = "\\d{11}".toRegex()
        private fun String.erGyldigIdentitetsnummer() = this.matches(identitetsnummerRegex)
        private fun String.erGyldigJournalpostId() = this.matches(journalpostIdRegex)
        internal const val Navn = "OverføreOmsorgsdager"
    }
}
