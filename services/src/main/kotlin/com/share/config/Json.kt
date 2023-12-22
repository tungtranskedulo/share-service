package com.share.config

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.node.*
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.share.common.toIsoString
import java.math.BigDecimal
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

val objectMapper = Json.mapper
inline fun <reified T> T.toJsonObj() = objectMapper.convertValue(this, JsonObject::class.java)
inline fun <reified T> T.toJson() = objectMapper.writeValueAsString(this)
inline fun <reified T> String.fromJson(): T = objectMapper.readValue(this)
inline fun <reified T> ObjectMapper.readValue(content: String): T = readValue(content, jacksonTypeRef<T>())

typealias JsonObject = ObjectNode
typealias JsonArray = ArrayNode


fun emptyJsonObject(): ObjectNode {
    return ObjectMapper().createObjectNode()
}
fun emptyJsonArrayObject(): ArrayNode {
    return ObjectMapper().createArrayNode()
}

object Json {

    private object InstantSerializerWithMilliSecondPrecision :
        InstantSerializer(INSTANCE, false, false, DateTimeFormatterBuilder().appendInstant(3).toFormatter())

    private val ZonedDateTimeSerializerWithMilliSecondPrecision =
        ZonedDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"))

    val mapper: ObjectMapper = jacksonMapperBuilder()
        .addModule(JavaTimeModule().apply {
            addSerializer(Instant::class.java, InstantSerializerWithMilliSecondPrecision)
            addSerializer(ZonedDateTime::class.java, ZonedDateTimeSerializerWithMilliSecondPrecision)
        })
        .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
        .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
        .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
        .build()


    val nodeFactory: JsonNodeFactory = JsonNodeFactory.withExactBigDecimals(true)

    val NULL: NullNode = NullNode.instance
    val TRUE: BooleanNode = BooleanNode.TRUE
    val FALSE: BooleanNode = BooleanNode.FALSE
    fun int(i: Int): IntNode = IntNode.valueOf(i)
    fun dbl(d: Double): DoubleNode = DoubleNode.valueOf(d)
    fun dec(d: BigDecimal): DecimalNode = DecimalNode.valueOf(d)
    fun str(s: String): TextNode = TextNode.valueOf(s)

    fun fromValue(a: Any?): JsonNode =
        if (a == null) NULL
        else {
            when (a.javaClass.kotlin) {
                Boolean::class    -> if (a as Boolean) TRUE else FALSE
                Int::class        -> int(a as Int)
                Double::class     -> dbl(a as Double)
                BigDecimal::class -> dec(a as BigDecimal)
                String::class     -> str(a as String)
                Instant::class    -> str((a as Instant).toIsoString())
                else -> throw IllegalStateException("Unknown generic value type: ${a.javaClass.kotlin}")
            }
        }

    fun arr(vararg children: JsonNode): JsonArray = ArrayNode(nodeFactory, listOf(*children))
    fun obj(vararg pairs: Pair<String, JsonNode>): JsonObject = ObjectNode(nodeFactory, mapOf(*pairs))

}
