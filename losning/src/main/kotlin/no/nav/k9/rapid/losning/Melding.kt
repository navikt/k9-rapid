package no.nav.k9.rapid.losning

import com.fasterxml.jackson.databind.node.ObjectNode
import de.huxhorn.sulky.ulid.ULID
import org.jetbrains.annotations.TestOnly

data class Melding internal constructor(private val rawJson: String) {
    private val json = Løsningsformat.jacksonObjectMapper.readTree(rawJson)
    private val id = try {
        ULID.parseULID(json.get(Løsningsformat.Id).asText())
    } catch (ignore: Throwable) { null }
    private val løsninger = json.get(Løsningsformat.Løsninger)

    fun harLøsningPå(behov: String) : Boolean {
        if (id == null) return false
        if (isNull(løsninger) || !løsninger.isObject) return false
        return (løsninger as ObjectNode).hasNonNull(behov)
    }

    fun harLøsningPå(løsningResolver: LøsningResolver<*>) = harLøsningPå(løsningResolver.behov())

    fun <Løsning>løsningPå(løsningResolver: LøsningResolver<Løsning>) : Pair<String, Løsning> = when(harLøsningPå(løsningResolver.behov())) {
        true -> Pair(id.toString(), løsningResolver.resolve(løsninger.get(løsningResolver.behov())))
        false -> throw IllegalStateException("Meldingen inneholder ikke løsning på ${løsningResolver.behov()}")
    }

    fun <Løsning>løsningPåOrNull(løsningResolver: LøsningResolver<Løsning>) : Pair<String, Løsning>? = when(harLøsningPå(løsningResolver.behov())) {
        true -> løsningPå(løsningResolver)
        false -> null
    }
}

fun String.somMelding() = Melding(this)

fun String.erNyereEnn(annen: String) =
    ULID.parseULID(this).timestamp() > ULID.parseULID(annen).timestamp()

@TestOnly
fun String.somMeldingMedTestLøsninger(testLøsninger: Map<String, *>) : Melding{
    val json = (Løsningsformat.jacksonObjectMapper.readTree(this) as ObjectNode)
    val løsninger = Løsningsformat.jacksonObjectMapper.createObjectNode()
    testLøsninger.forEach { (behov,testLøsning) ->
        løsninger.replace(behov, Løsningsformat.jacksonObjectMapper.valueToTree(testLøsning))
    }
    json.replace(Løsningsformat.Løsninger, løsninger)
    return Løsningsformat.jacksonObjectMapper.writeValueAsString(json).somMelding()
}