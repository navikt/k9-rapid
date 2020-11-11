package no.nav.k9.rapid.losning

import java.time.LocalDate
import java.time.ZonedDateTime

data class OverføreOmsorgsdagerLøsning(
        val løst: ZonedDateTime,
        val utfall: String,
        val begrunnelser: List<String> = listOf(),
        val overføringer: Map<String, Overføringer> = mapOf()) {

    fun erGjennomført() = utfall.equals("Gjennomført", ignoreCase = true)
    fun erAvslått() = utfall.equals("Avslått", ignoreCase = true)
    fun ikkeBehandlesAvNyttSystem() = !erGjennomført()  && !erAvslått()

    data class Navn(
            val fornavn: String,
            val mellomnavn: String?,
            val etternavn: String
    )

    data class Person(
            val navn: Navn?,
            val fødselsdato: LocalDate
    )

    data class OverføringFått(
            val fra: Person,
            val antallDager: Int,
            val gjelderFraOgMed: LocalDate,
            val gjelderTilOgMed: LocalDate
    )

    data class OverføringGitt(
            val til: Person,
            val antallDager: Int,
            val gjelderFraOgMed: LocalDate,
            val gjelderTilOgMed: LocalDate
    )

    data class Overføringer(
            val fått: List<OverføringFått>,
            val gitt: List<OverføringGitt>
    )
}