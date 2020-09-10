package no.nav.k9.rapid.river

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.TextNode
import no.nav.helse.rapids_rivers.JsonMessage
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
        jsonMessage.interestedIn(Løsninger)
        true
    } else {
        jsonMessage.requireValue(Behovsformat.Type, StøttetTypeOgVersjon.first.asText())
        jsonMessage.requireValue(Behovsformat.Versjon, StøttetTypeOgVersjon.first.asText())
        false
    }
}

fun JsonMessage.skalLøseBehov(behov: String) {
    if (!erBehovssekvens(this)) return

    val behovsrekkefølge = get(Behovsformat.Behovsrekkefølge) as ArrayNode

    val behovIndex = behovsrekkefølge.indexOf(TextNode(behov))

    if (behovIndex == -1) {
        requireContains(Behovsformat.Behovsrekkefølge, behov)
        return
    }

    val forrigeBehov = when (behovIndex == 0) {
        true -> null
        false -> behovsrekkefølge[behovIndex-1].asText()
    }

    val løsninger = get(Løsninger)

    when {
        // Allerede løst
        løsninger.hasNonNull(behov) -> {
            require("$Løsninger.$behov") { throw IllegalStateException("Behov allerede løst.") }
            return
        }
        // Skal løses nå
        forrigeBehov != null && løsninger.hasNonNull(forrigeBehov) -> return
        // Venter på andre behov
        else -> {
            behovsrekkefølge.map { it.asText() }.forEach {
                if (it == behov) return
                requireKey("$Løsninger.$it")
            }
        }
    }
}