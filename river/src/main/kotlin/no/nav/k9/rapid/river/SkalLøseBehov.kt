package no.nav.k9.rapid.river

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.isMissingOrNull
import no.nav.k9.rapid.behov.Behovsformat
import no.nav.k9.rapid.behov.Behovssekvens

internal const val Løst = "løst"
internal const val Løsninger = "@løsninger"
private val StøttetTypeOgVersjon = Pair(
        TextNode(Behovsformat.BehovssekvensType), TextNode(Behovsformat.BehovssekvensVersjon)
)

internal fun erBehovssekvens(jsonMessage: JsonMessage) : Boolean {
    jsonMessage.interestedIn(Behovsformat.Type, Behovsformat.Versjon)
    val typeOgVersjon = Pair(
            jsonMessage[Behovsformat.Type], jsonMessage[Behovsformat.Versjon]
    )

    return if (typeOgVersjon == StøttetTypeOgVersjon) {
        Behovssekvens.demandedKeys.forEach { jsonMessage.demandKey(it) }
        Behovssekvens.demandedValues.forEach { (key, value) -> jsonMessage.demandValue(key, value)}
        jsonMessage.interestedIn(Løsninger, Behovsformat.SistEndret)
        true
    } else {
        jsonMessage.requireValue(Behovsformat.Type, StøttetTypeOgVersjon.first.asText())
        jsonMessage.requireValue(Behovsformat.Versjon, StøttetTypeOgVersjon.first.asText())
        false
    }
}

fun JsonMessage.skalLøseBehov(behov: String) : String? {
    if (!erBehovssekvens(this)) return null

    val behovsrekkefølge = (get(Behovsformat.Behovsrekkefølge) as ArrayNode).map { it.asText() }

    val behovIndex = behovsrekkefølge.indexOfFirst { it == behov || it.startsWith("${behov}@") }

    if (behovIndex == -1) {
        requireContains(Behovsformat.Behovsrekkefølge, behov)
        return null
    }

    val aktueltBehov = behovsrekkefølge[behovIndex]

    val forrigeBehov = when (behovIndex == 0) {
        true -> null
        false -> behovsrekkefølge[behovIndex-1]
    }

    val løsninger = get(Løsninger)

    when {
        // Allerede løst
        løsninger.hasNonNull(aktueltBehov) -> {
            require("$Løsninger.$aktueltBehov") { throw IllegalStateException("Behov allerede løst.") }
            return null
        }
        // Skal løses nå
        behovIndex == 0 || (forrigeBehov != null && løsninger.hasNonNull(forrigeBehov)) -> return aktueltBehov
        // Venter på andre behov
        else -> {
            behovsrekkefølge.forEach {
                if (it == aktueltBehov) return null
                requireKey("$Løsninger.$it")
            }
        }
    }

    return null
}

fun JsonMessage.aktueltBehovOrNull() : String? {
    require(erBehovssekvens(this)) { "Meldingen er ikke en behovssekvens" }
    val behovsrekkefølge = (get(Behovsformat.Behovsrekkefølge) as ArrayNode).map { it.asText() }
    val løsninger = when (get(Løsninger).isMissingOrNull()) {
        true -> emptyList()
        false -> (get(Løsninger) as ObjectNode).fieldNames().asSequence().toList()
    }
    return behovsrekkefølge.firstOrNull { !løsninger.contains(it) }
}

fun JsonMessage.aktueltBehov() = requireNotNull(aktueltBehovOrNull()) {
    "Feil bruk av aktueltBehov, meldingen har inget aktuelt behov."
}