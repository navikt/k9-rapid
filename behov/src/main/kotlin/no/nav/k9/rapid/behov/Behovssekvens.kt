package no.nav.k9.rapid.behov

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import de.huxhorn.sulky.ulid.ULID
import no.nav.k9.rapid.behov.Behovsformat.iso8601
import org.intellij.lang.annotations.Language

class Behovssekvens(
        id: String,
        correlationId: String,
        vararg behov: Behov) {
    private val opprettet = Behovsformat.nå()
    private val forventetLøst = behov.forventerLøsningInnen().let { opprettet.plus(it) }

    @Language("json")
    private val rawJson = """
    {
      "${Behovsformat.Id}": "$id",
      "${Behovsformat.Type}": "${Behovsformat.BehovssekvensType}",
      "${Behovsformat.Versjon}": "${Behovsformat.BehovssekvensVersjon}",
      "${Behovsformat.Opprettet}" : "${opprettet.iso8601()}",
      "${Behovsformat.ForventetLøst}" : "${forventetLøst.iso8601()}",
      "${Behovsformat.CorrelationId}": "$correlationId",
      "${Behovsformat.Behovsrekkefølge}": [],
      "${Behovsformat.Behov}": {}
    }
    """.trimIndent()

    private val json = (Behovsformat.jacksonObjectMapper.readTree(rawJson) as ObjectNode).also {
        behov.forEach { b ->
            b.valider()
            (it[Behovsformat.Behovsrekkefølge] as ArrayNode).add(b.navn)
            (it[Behovsformat.Behov] as ObjectNode).replace(b.navn, b.json)
        }
    }

    val keyValue = Pair(id, json.toString())

    init {
        val behovsrekkefølgeSize = json[Behovsformat.Behovsrekkefølge].size()
        val behovSize = json[Behovsformat.Behov].size()

        correlationId.validerCorrelationId()
        id.validerId()

        require(behovsrekkefølgeSize == behovSize) {
            "Meldingen inneholder $behovsrekkefølgeSize behov i ${Behovsformat.Behovsrekkefølge} og $behovSize i ${Behovsformat.Behov}"
        }
    }

    companion object {
        private val correlationIdRegex = "[a-zA-Z0-9_.\\-]{10,300}".toRegex()
        private fun String.validerCorrelationId() = require(this.matches(correlationIdRegex)) {
            "Ugyldig correlationId"
        }
        private fun String.validerId() = ULID.parseULID(this)

        val demandedKeys = listOf(
                Behovsformat.Id,
                Behovsformat.CorrelationId,
                Behovsformat.Behovsrekkefølge,
                Behovsformat.Behov,
                Behovsformat.Opprettet,
                Behovsformat.ForventetLøst
        )
        val demandedValues = mapOf(
                Behovsformat.Type to Behovsformat.BehovssekvensType,
                Behovsformat.Versjon to Behovsformat.BehovssekvensVersjon
        )
    }

    override fun toString() = keyValue.toString()
}