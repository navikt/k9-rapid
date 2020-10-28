package no.nav.k9.rapid.river

typealias Environment = Map<String, String>

fun Environment.harEnv(key: String) = containsKey(key)

fun Environment.hentRequiredEnv(key: String) : String = requireNotNull(get(key)) {
    "Environment variable $key må være satt"
}

fun Environment.hentOptionalEnv(key: String) : String? = get(key)

fun String.csvTilListe() = replace(" ", "")
    .split(",")

fun String.csvTilSet() = csvTilListe().toSet()