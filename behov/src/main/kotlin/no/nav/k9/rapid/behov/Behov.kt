package no.nav.k9.rapid.behov

import java.time.Duration
import java.time.Duration.ofHours

open class Behov(
        val navn: String,
        input: Map<String, *> = mapOf<String,Any>()) {
    val json = Behovsformat.jacksonObjectMapper.readTree(
            Behovsformat.jacksonObjectMapper.writeValueAsString(input)
    )

    open fun mangler(): List<String> = emptyList()

    open fun forventerLøsningInnen() : Duration = ofHours(2)

    fun valider() {
        if (mangler().isNotEmpty()) throw IllegalArgumentException("Ugyldig behov '$navn': ${mangler().joinToString(", ")}")
    }
}

fun Array<out Behov>.forventerLøsningInnen() = map { it.forventerLøsningInnen() }.maxOrNull()!!