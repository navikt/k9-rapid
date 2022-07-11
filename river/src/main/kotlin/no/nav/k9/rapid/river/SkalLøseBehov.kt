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
private val StøttetTypeOgVersjon = TextNode(Behovsformat.BehovssekvensType) to TextNode(Behovsformat.BehovssekvensVersjon)

internal fun erBehovssekvens(jsonMessage: JsonMessage) : Boolean {
    jsonMessage.interestedIn(Behovsformat.Type, Behovsformat.Versjon)
    val typeOgVersjon = jsonMessage[Behovsformat.Type] to jsonMessage[Behovsformat.Versjon]

    if (typeOgVersjon != StøttetTypeOgVersjon) {
        jsonMessage.requireValue(Behovsformat.Type, StøttetTypeOgVersjon.first.asText())
        jsonMessage.requireValue(Behovsformat.Versjon, StøttetTypeOgVersjon.second.asText())
        return false
    }

    jsonMessage.interestedIn(Behovsformat.BehovssekvensId)
    val behovssekvensResult = runCatching { jsonMessage.behovssekvensId() }
    if (behovssekvensResult.isFailure) {
        jsonMessage.require(Behovsformat.BehovssekvensId) { behovssekvensResult.getOrThrow() }
        return false
    }

    Behovssekvens.demandedKeys.forEach { jsonMessage.demandKey(it) }
    jsonMessage.interestedIn(Løsninger, Behovsformat.SistEndret)
    return true
}

private fun String.erMatchendeBehov(behov: String) =
    this == behov || this.startsWith("${behov}@")

fun JsonMessage.skalLøseBehov(behov: String) : String? {
    if (!erBehovssekvens(this)) return null

    val behovsrekkefølge = (get(Behovsformat.Behovsrekkefølge) as ArrayNode).map { it.asText() }
    val løsninger = get(Løsninger)

    // Behovet finnes ikke i behovsrekkefølgen
    if (behovsrekkefølge.firstOrNull { it.erMatchendeBehov(behov) } == null) {
        requireContains(Behovsformat.Behovsrekkefølge, behov)
        return null
    }

    // Finner index for første match som ikke har en løsning
    val uløstBehovIndex = behovsrekkefølge.indexOfFirst {
        it.erMatchendeBehov(behov) && !løsninger.hasNonNull(it)
    }

    // Om det ikke finnes noen index for første match som ikke har løsning er alle matchende behov løst
    if (uløstBehovIndex == -1) {
        require("$Løsninger.$behov") { throw IllegalStateException("Alle matchende behov allerede løst.") }
        return null
    }

    val uløstBehov = behovsrekkefølge[uløstBehovIndex]

    val forrigeBehov = when (uløstBehovIndex == 0) {
        true -> null
        false -> behovsrekkefølge[uløstBehovIndex-1]
    }

    when {
        // Skal løses nå
        uløstBehovIndex == 0 || (forrigeBehov != null && løsninger.hasNonNull(forrigeBehov)) -> return uløstBehov
        // Venter på andre behov
        else -> {
            behovsrekkefølge.forEach {
                if (it == uløstBehov) return null
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