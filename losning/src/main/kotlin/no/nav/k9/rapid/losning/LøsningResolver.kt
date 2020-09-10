package no.nav.k9.rapid.losning

import com.fasterxml.jackson.databind.JsonNode

interface LøsningResolver<Løsning> {
    fun behov() : String
    fun resolve(jsonNode: JsonNode) : Løsning
}