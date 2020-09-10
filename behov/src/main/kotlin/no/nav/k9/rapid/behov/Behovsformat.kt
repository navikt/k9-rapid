package no.nav.k9.rapid.behov

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ofPattern

object Behovsformat {
    internal val jacksonObjectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    const val Id = "@id"
    internal const val CorrelationId = "@correlationId"
    const val Behovsrekkefølge = "@behovsrekkefølge"
    const val Behov = "@behov"
    internal const val Opprettet = "@opprettet"
    const val ForventetLøst = "@forventetLøst"

    const val Type = "@type"
    const val Versjon = "@versjon"

    val BehovssekvensType = "Behovssekvens"
    val BehovssekvensVersjon = "1"

    private val ISO8601 = ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private val UTC = ZoneId.of("UTC")

    fun nå() = ZonedDateTime.now(UTC)
    fun ZonedDateTime.iso8601() = ISO8601.format(this)
}

