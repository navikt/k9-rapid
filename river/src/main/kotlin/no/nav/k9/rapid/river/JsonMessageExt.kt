package no.nav.k9.rapid.river

import com.fasterxml.jackson.databind.node.TextNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.isMissingOrNull
import de.huxhorn.sulky.ulid.ULID
import no.nav.k9.rapid.behov.Behovsformat

internal fun JsonMessage.getString(key: String) = get(key).also {
    if (it.isMissingOrNull()) throw IllegalStateException("Mangler $key")
    if (it !is TextNode) throw IllegalStateException("$key må være String")
}.let { requireNotNull(it.asText()) }

fun JsonMessage.behovssekvensId() = getString(Behovsformat.BehovssekvensId).also {
    ULID.parseULID(it)
}

internal fun JsonMessage.correlationId() = getString(Behovsformat.CorrelationId)
