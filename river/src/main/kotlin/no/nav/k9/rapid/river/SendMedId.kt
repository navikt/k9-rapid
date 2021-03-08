package no.nav.k9.rapid.river

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.k9.rapid.behov.Behovsformat
import no.nav.k9.rapid.behov.Behovsformat.iso8601
import no.nav.k9.rapid.behov.Behovsformat.nå

fun JsonMessage.oppdaterSistEndret() {
    set(Behovsformat.SistEndret, nå().iso8601())
}

fun MessageContext.sendMedId(packet: JsonMessage) {
    packet.oppdaterSistEndret()
    publish(packet.behovssekvensId(), packet.toJson())
}