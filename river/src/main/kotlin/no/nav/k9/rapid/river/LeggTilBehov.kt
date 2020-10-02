package no.nav.k9.rapid.river

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.isMissingOrNull
import no.nav.k9.rapid.behov.Behov
import no.nav.k9.rapid.behov.Behovsformat

fun JsonMessage.leggTilBehov(
    aktueltBehov: String,
    vararg behov: Behov) = leggTilNyeBehov(
    aktueltBehov = aktueltBehov,
    validerLøsningPåAktueltBehov = { løsninger ->
        require(løsninger.isMissingOrNull() || !løsninger.hasNonNull(aktueltBehov)) {
            "Det aktuelle behovet '$aktueltBehov' er allerede løst. Kan ikke legge til behov før."
        }
    },
    hentNyBehovsrekkefølge = { før, etter, nye ->
        før + nye + aktueltBehov + etter
    },
    behov = behov
)

fun JsonMessage.leggTilBehovEtter(
    aktueltBehov: String,
    vararg behov: Behov) = leggTilNyeBehov(
    aktueltBehov = aktueltBehov,
    validerLøsningPåAktueltBehov = { løsninger ->
        require(!løsninger.isMissingOrNull() && løsninger.hasNonNull(aktueltBehov)) {
            "Det aktuelle behovet '$aktueltBehov' er ikke løst. Kan ikke legge til behov etter."
        }
    },
    hentNyBehovsrekkefølge = { før, etter, nye ->
        før + aktueltBehov + nye + etter
    },
    behov = behov
)

private fun JsonMessage.leggTilNyeBehov(
    aktueltBehov: String,
    validerLøsningPåAktueltBehov: (JsonNode) -> Unit,
    hentNyBehovsrekkefølge: (List<String>, List<String>, List<String>) -> List<String>,
    vararg behov: Behov) : JsonMessage {
    require(behov.isNotEmpty()) {
        "Må legges til minst et nytt behov."
    }

    val nåværendeBehovsrekkefølge = (get(Behovsformat.Behovsrekkefølge) as ArrayNode).map { it.asText()!! }
    val nyeBehovsrekkefølge = behov.map { it.navn }
    val duplikateBehov = nåværendeBehovsrekkefølge.intersect(nyeBehovsrekkefølge)

    require(duplikateBehov.isEmpty()) {
        "Behov finnes allerede: [${duplikateBehov.joinToString(",")}]"
    }

    val index = nåværendeBehovsrekkefølge.indexOf(aktueltBehov)
    require(index != -1) {
        "Behovsrekkefølgen inneholder ikke $aktueltBehov"
    }

    get(Løsninger).also(validerLøsningPåAktueltBehov)

    val behovFør = when (index) {
        0 -> emptyList()
        else -> nåværendeBehovsrekkefølge.subList(0, index)
    }

    val behovEtter = when (nåværendeBehovsrekkefølge.last() == aktueltBehov) {
        true -> emptyList()
        false -> nåværendeBehovsrekkefølge.subList(index+1, nåværendeBehovsrekkefølge.size)
    }


    val oppdatertBehovsrekkefølge = hentNyBehovsrekkefølge(
        behovFør,
        behovEtter,
        nyeBehovsrekkefølge
    )

    set(Behovsformat.Behovsrekkefølge, oppdatertBehovsrekkefølge)

    val nåværendeBehov = get(Behovsformat.Behov) as ObjectNode

    behov.forEach { b ->
        nåværendeBehov.replace(b.navn, b.json)
    }

    set(Behovsformat.Behov, nåværendeBehov)

    return this
}