package com.share.service.graphql


import com.share.service.common.parseIsoInstant
import com.share.service.config.Json
import java.time.Instant
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties


object GraphQLFieldsHandler {

    private const val ResourceId = "resourceId"
    private const val Start = "start"
    private const val Finish = "finish"
    private const val Status = "status"
    private const val IsAvailable = "isAvailable"
    private const val AvailabilityType = "availabilityType"

    @Suppress("UNCHECKED_CAST")
    fun <V> buildAvailabilities(map: Map<String, V?>): Map<String, Any?> {
        val schema = with(map){
            AvailabilitiesModel(
                UID = (this["UID"] as String?).orEmpty(),
                ResourceId = (this[ResourceId] as String?).orEmpty(),
                Start = (this[Start] as String?).orEmpty(),
                Finish = (this[Finish] as String?).orEmpty(),
                Status = (this[Status] as String?)?.let { AvailabilityStatus.valueOf(it).toBackendName() }.orEmpty(),
                IsAvailable = (this[IsAvailable] as Boolean?) ?: false,
                Type = (this[AvailabilityType] as String?).orEmpty(),
                Notes = (this["Notes"] as String?).orEmpty(),
            )
        }

        return flattenObjToMap(schema)
    }

    @Suppress("UNCHECKED_CAST")
    private fun convertModelToMap(model: EventModel): Map<String, Any?> {
        val map = Json.mapper.convertValue(model, Map::class.java) as Map<String, Any?>
        if (map["UID"] as String? == null) {
            map.toMutableMap().remove("UID")
        }
        return map
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> flattenObjToMap(obj: T): Map<String, Any> {
        return (obj::class as KClass<T>).memberProperties.associate { prop ->
            (prop.name to prop.get(obj)?.let { value ->
                if (value::class.isData) {
                    flattenObjToMap(value)
                } else {
                    value
                }
            })
        } as Map<String, Any>
    }

}