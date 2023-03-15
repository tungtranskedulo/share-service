package com.share.services.graphql

import com.fasterxml.jackson.annotation.JsonValue
import com.share.services.graphql.GraphQLMutationFactory.Companion.schemaBuilder
import com.share.services.graphql.GraphQLMutationFactory.Keys.UID
import com.share.services.graphql.GraphQLSchemaBuilder.buildAccount
import com.share.services.graphql.GraphQLSchemaBuilder.buildActivity
import com.share.services.graphql.GraphQLSchemaBuilder.buildAvailability
import com.share.services.graphql.GraphQLSchemaBuilder.buildContact
import com.share.services.graphql.GraphQLSchemaBuilder.buildDefaultEventModel
import com.share.services.graphql.GraphQLSchemaBuilder.buildJob
import com.share.services.graphql.GraphQLSchemaBuilder.buildJobAllocation
import com.share.services.graphql.GraphQLSchemaBuilder.buildJobTask
import com.share.services.graphql.GraphQLSchemaBuilder.buildResources
import mu.KotlinLogging
import java.util.*
import kotlin.math.log

private val log = KotlinLogging.logger {}

data class ObjectId(@get:JsonValue val value: String) {
    override fun toString() = value
}

class GraphQLMutationFactory {

    object Keys {
        const val UID = "UID"
        const val CUSTOM_FIELDS = "customFields"
    }

    companion object {
        val schemaBuilder = mapOf(
            Schema.Accounts.name to SchemaBuilder(::buildAccount),
            Schema.Activities.name to SchemaBuilder(::buildActivity),
            Schema.Contacts.name to SchemaBuilder(::buildContact),
            Schema.JobTasks.name to SchemaBuilder(::buildJobTask),
            Schema.Resources.name to SchemaBuilder(::buildResources),
            Schema.Availabilities.name to SchemaBuilder(::buildAvailability),
            Schema.Jobs.name to SchemaBuilder(::buildJob),
            Schema.JobAllocations.name to SchemaBuilder(::buildJobAllocation),
        )
    }

    fun toUpdateMutation(
        objectType: Schema,
        id: ObjectId,
        eventSchemaVersion: Int = 1,
        eventModel: Map<String, Any?>
    ): GraphQLMutation {
        log.info { eventSchemaVersion }
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
        log.info { eventSchemaVersion }
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
        eventModel: Map<String, Any?>,
    ): Map<String, Any?> {
        return eventModel.toMutableMap()
            .also { if (id != null) it[UID] = id.value }
            .mapKeys { entry -> entry.key.replaceFirstChar { it.titlecase() } }
            .handleSchema(schema)
            .liftCustomFields()
    }

}

@Suppress("UNCHECKED_CAST")
fun <K, V> Map<K, V>.filterNullValues(): MutableMap<K, V> {
    return this.filter { it.value != null }.toMutableMap()
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
    return schemaBuilder[schema.name]?.let { it(this) } ?: buildDefaultEventModel(this)
}

data class GraphQLMutation(
    val schemaName: String,
    val mutationName: String,
    val inputType: String,
    val keysAndValues: Any,
    val inputVariable: String = "input",
    val parameterName: String = "input",
)

fun defaultUpdateMutationType(schemaName: String) = "Update$schemaName"
fun defaultInsertMutationType(schemaName: String) = "New$schemaName"
fun defaultDeleteMutationType() = "ID"
fun defaultUpdateMutationName(schemaName: String): String = "update$schemaName"
fun defaultInsertMutationName(schemaName: String): String = "insert$schemaName"
fun defaultDeleteMutationName(schemaName: String): String = "delete$schemaName"

//fun GraphQLMutation.toGraphQLQuery(): GraphQLUtils.GraphQLQuery {
//    val inputVariableWithDollar = "\$${inputVariable}"
//    return GraphQLUtils.GraphQLQuery(
//        query = """
//            mutation $mutationName($inputVariableWithDollar: $inputType!) {
//              schema {
//              	$mutationName($parameterName: $inputVariableWithDollar)
//              }
//            }
//        """.trimIndent(),
//        variables = mapOf(inputVariable to keysAndValues)
//    )
//}


