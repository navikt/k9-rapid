package no.nav.k9.rapid.behov

open class Behov(
        val navn: String,
        input: Map<String, *> = mapOf<String,Any>()) {
    val json = Behovsformat.jacksonObjectMapper.readTree(
            Behovsformat.jacksonObjectMapper.writeValueAsString(input)
    )

    open fun mangler(): List<String> = emptyList()

    fun valider() {
        if (mangler().isNotEmpty()) throw IllegalArgumentException("Ugyldig behov '$navn': ${mangler().joinToString(", ")}")
    }
}