package com.share.service.graphql

import com.fasterxml.jackson.annotation.JsonValue
import com.share.service.config.Json
import com.share.service.graphql.GraphQLFieldsHandler.buildAvailabilities
import com.share.service.graphql.GraphQLMutationFactory.Keys.UID
import java.util.*

data class ObjectId(@get:JsonValue val value: String) {
    override fun toString() = value
}

class GraphQLMutationFactory {

    object Keys {
        const val UID = "UID"
        const val STATUS = "Status"
        const val JOB_STATUS = "JobStatus"
        const val NOTIFICATION_TYPE = "NotificationType"
        const val AVAILABILITY_TYPE = "AvailabilityType"
        const val TYPE = "Type"
        const val CUSTOM_FIELDS = "customFields"
    }

    fun toUpdateMutation(
        objectType: Schema,
        id: ObjectId,
        eventSchemaVersion: Int = 1,
        eventModel: Map<String, Any?>
    ): GraphQLMutation {
        val graphQLModel = toGraphQLFieldsV1(objectType, id, eventModel)
        return GraphQLMutation(
            schemaName = objectType.name,
            mutationName = defaultUpdateMutationName(objectType.name),
            inputType = defaultUpdateMutationType(objectType.name),
            keysAndValues = graphQLModel
        )
    }

    fun toInsertMutation(
        objectType: Schema,
        eventSchemaVersion: Int = 1,
        eventModel: Map<String, Any?>
    ): GraphQLMutation {
        val graphQLModel = toGraphQLFieldsV1(objectType, null, eventModel)
        return GraphQLMutation(
            schemaName = objectType.name,
            mutationName = defaultInsertMutationName(objectType.name),
            inputType = defaultInsertMutationType(objectType.name),
            keysAndValues = graphQLModel
        )
    }

    private fun toGraphQLFieldsV1(
        schema: Schema,
        id: ObjectId?,
        eventModel: Map<String, Any?>
    ): Map<String, Any?> {
        val map = eventModel.toMutableMap()
            .also {
                if (id != null) it[Keys.UID] = id.value
            }
            .liftCustomFields()
            .handleSchema(schema)
            .removeNullValuesByKey(Keys.UID)

        println("map $map")
        return map
    }

}

@Suppress("UNCHECKED_CAST")
fun <K, V> Map<K, V>.filterNullValues(): MutableMap<K, V> {
    return this.filter { it.value != null }.toMutableMap()
}

@Suppress("UNCHECKED_CAST")
fun <K, V> Map<K, V>.removeNullValuesByKey(key: K): MutableMap<K, V> {
    val map = this.toMutableMap()
    if ((map[key] as String).isNullOrEmpty()) {
        map.remove(key)
    }
    return map
}

/**
 * Lifts the CustomField nested properties up to the root level, allowing nulls through
 */
@Suppress("UNCHECKED_CAST")
fun <V> Map<String, V?>.liftCustomFields(): Map<String, V?> {
    val map = this.toMutableMap()
    with(GraphQLMutationFactory.Keys) {
        // Ignore CustomFields if the object itself is null, if the key is present and null remove it
        if (!map.containsKey(CUSTOM_FIELDS) || map[CUSTOM_FIELDS] == null) {
            return map.also { it.remove(CUSTOM_FIELDS) }
        }

        // Lift each item to the root of the map, allowing the item values to be null as needed!
        val customFields = (map[CUSTOM_FIELDS] as Map<String, V?>)
        customFields.forEach {
            map[it.key] = it.value
        }

        // Remove the source object, as all of its properties have now been moved to the root level.
        map.remove(CUSTOM_FIELDS)
        return map
    }
}

@Suppress("UNCHECKED_CAST")
fun <V> Map<String, V?>.handleSchema(schema: Schema): Map<String, Any?> {
    val map = this.toMutableMap()
    val handler = initSchemaHandler(map)
    return handler[schema.name]?.invoke() ?: map
}

private fun <V> initSchemaHandler(map: Map<String, V?>): Map<String, () -> Map<String, Any?>> {
    val commands = mutableMapOf<String, () -> Map<String, Any?>>()
    commands[Schema.Availabilities.name] = { buildAvailabilities(map) }

    return commands
}

