package no.nav.k9.rapid.river

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

fun JsonNode.requireText() = require(isTextual) { "Må være tekst." }
fun JsonNode.requireInt() = require(isIntegralNumber) { "Må være int." }
fun JsonNode.requireBoolean() = require(isBoolean) { "Må være boolean." }
fun JsonNode.requireArray(predicate: (JsonNode) -> Boolean = { true }) = require(isArray && all { predicate(it) }) { "Må være array." }
fun JsonNode.requireObject(predicate: (ObjectNode) -> Boolean = { true }) = require(isObject && predicate(this as ObjectNode))