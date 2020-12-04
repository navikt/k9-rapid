package no.nav.k9.rapid.losning

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.k9.rapid.behov.OverføreKoronaOmsorgsdagerBehov

class OverføreKoronaOmsorgsdagerLøsningResolver internal constructor() : LøsningResolver<OverføreKoronaOmsorgsdagerLøsning> {
    override fun behov() = OverføreKoronaOmsorgsdagerBehov.Navn

    override fun resolve(jsonNode: JsonNode) : OverføreKoronaOmsorgsdagerLøsning {
        return jacksonObjectMapper.convertValue(jsonNode)
    }

    companion object {
        private val jacksonObjectMapper = jacksonObjectMapper()
                .registerModule(JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

        val Instance = OverføreKoronaOmsorgsdagerLøsningResolver()
    }
}