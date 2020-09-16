package no.nav.k9.rapid.river

import no.nav.helse.rapids_rivers.JsonMessage

fun JsonMessage.harLøsningPåBehov(vararg behov: String) {
    if (!erBehovssekvens(this)) return
    require(behov.isNotEmpty()) { "Må sendes med minst et behov." }

    val løsninger = get(Løsninger).fieldNames().asSequence().toList()

    val manglerLøsningPå = behov.toList().subtract(løsninger)

    when (manglerLøsningPå.isEmpty()) {
        true -> return
        false -> {
            manglerLøsningPå.forEach {
                requireKey("$Løsninger.$it")
            }
        }
    }
}

fun JsonMessage.utenLøsningPåBehov(vararg behov: String) {
    if (!erBehovssekvens(this)) return
    require(behov.isNotEmpty()) { "Må sendes med minst et behov." }

    val løsninger = get(Løsninger).fieldNames().asSequence().toList()
    val finnesLøsningPå = behov.toList().intersect(løsninger)

    when (finnesLøsningPå.isEmpty()) {
        true -> return
        false -> {
            finnesLøsningPå.forEach {
                require("$Løsninger.$it") { throw IllegalStateException("Behov har en løsning.") }
            }
        }
    }
}