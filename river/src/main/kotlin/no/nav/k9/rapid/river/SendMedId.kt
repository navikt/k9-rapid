package no.nav.k9.rapid.river

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.k9.rapid.behov.Behovsformat

fun RapidsConnection.MessageContext.sendMedId(jsonMessage: JsonMessage) {
    send(jsonMessage[Behovsformat.Id].asText(), jsonMessage.toJson())
}