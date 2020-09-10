package no.nav.k9.rapid.river

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.isMissingOrNull
import no.nav.k9.rapid.behov.Behovsformat.iso8601
import no.nav.k9.rapid.behov.Behovsformat.nå

private val jacksonObjectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

fun JsonMessage.leggTilLøsning(
        behov: String,
        løsning: Map<String, *> = emptyMap<String, Any>()) {
    val nåværendeLøsninger = get(Løsninger)

    val løsningMedTimestamp = løsning.plus(Løst to nå().iso8601())
    if (nåværendeLøsninger.isMissingOrNull()) {
        set(Løsninger, mapOf(behov to løsningMedTimestamp))
    } else if (nåværendeLøsninger.isObject) {
        val løsninger = nåværendeLøsninger as ObjectNode
        løsninger.replace(behov, jacksonObjectMapper.valueToTree(løsningMedTimestamp))
        set(Løsninger, løsninger)
    }
}