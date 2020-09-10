package no.nav.k9.rapid.river

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.isMissingOrNull
import no.nav.k9.rapid.behov.Behov
import no.nav.k9.rapid.behov.Behovsformat

fun JsonMessage.leggTilBehov(
        aktueltBehov: String,
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

    val løsninger = get(Løsninger)
    require(løsninger.isMissingOrNull() || !løsninger.hasNonNull(aktueltBehov)) {
        "Det aktuelle behovet '$aktueltBehov' er  allerede løst"
    }


    val oppdatertBehovsrekkefølge = when (index) {
        0 -> nyeBehovsrekkefølge + nåværendeBehovsrekkefølge
        else -> nåværendeBehovsrekkefølge.subList(0,index) +
                nyeBehovsrekkefølge +
                nåværendeBehovsrekkefølge.subList(index, nåværendeBehovsrekkefølge.size)
    }

    set(Behovsformat.Behovsrekkefølge, oppdatertBehovsrekkefølge)

    val nåværendeBehov = get(Behovsformat.Behov) as ObjectNode

    behov.forEach { b ->
        nåværendeBehov.replace(b.navn, b.json)
    }

    set(Behovsformat.Behov, nåværendeBehov)
    return this
}