package no.nav.k9.rapid.losning

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

internal object Løsningsformat {
    internal val jacksonObjectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    internal const val Id = "@id"
    const val Løsninger = "@løsninger"
}

fun isNull(jsonNode: JsonNode?) = jsonNode == null || jsonNode.isNull || jsonNode.isMissingNode