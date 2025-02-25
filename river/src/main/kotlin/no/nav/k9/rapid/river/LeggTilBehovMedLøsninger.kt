package no.nav.k9.rapid.river

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import no.nav.k9.rapid.behov.Behov

fun JsonMessage.leggTilBehovMedLøsninger(
    aktueltBehov: String,
    vararg behovMedLøsninger: Pair<Behov, Map<String,*>>) : JsonMessage {
    leggTilBehov(
        aktueltBehov = aktueltBehov,
        behov = behovMedLøsninger.map { it.first }.toTypedArray()
    )
    behovMedLøsninger.forEach { (behov, løsning) ->
        leggTilLøsning(
            behov = behov.navn,
            løsning = løsning
        )
    }
    return this
}