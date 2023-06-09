package com.share.model

import arrow.core.Option
import arrow.core.firstOrNone
import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.node.ObjectNode
import com.share.config.JsonObject
import java.nio.charset.StandardCharsets
import java.time.Instant

data class TenantId(@get:JsonValue val value: String) { override fun toString() = value }

// the id of the user in Auth0
data class UserId(@get:JsonValue val value: String) { override fun toString() = value }

// the id of the user in Salesforce/Standalone
data class VendorUserId(@get:JsonValue val value: String) { override fun toString() = value }

data class ResourceId(@get:JsonValue val value: String) { override fun toString() = value }

data class DeviceId(@get:JsonValue val value: String) {
    override fun toString() = value
}

data class EventId(@get:JsonValue val value: String) {
    override fun toString() = value
}
data class ObjectId(@get:JsonValue val value: String) {
    override fun toString() = value
}
data class MexInstance(val id: String, val name: String)

data class MexInstanceData(val id: String, val engine: String)

data class NextEventQueryResult(
    val id: String,
    val eventJson: JsonObject,
)

data class EventQueryResult(
    val eventId: String,
    val clientSequence: Long,
)

data class ClientEvent(
    @JsonAlias("_id")
    val eventId: EventId,
    val timeWritten: Instant,
    val timeProcessed: Instant? = null,
    val timeLastUpdated: Instant? = null,
    val tenantId: TenantId,
    val resourceId: ResourceId,
    val clientSequence: Long,
    val deviceIdentifier: DeviceId,
    val sessionIdentifier: String? = null,
    val eventSchemaVersion: Int,
    val data: Data,
) {
    val type = "ClientEvent"

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
    @JsonSubTypes(
        JsonSubTypes.Type(value = Data.WriteData::class),
        JsonSubTypes.Type(value = Data.JobOfferResponseData::class),
        JsonSubTypes.Type(value = Data.MexWriteData::class),
        JsonSubTypes.Type(value = Data.MalFormedData::class)
    )
    sealed class Data {
        abstract val type: ClientEventDataType

        // JsonTypeName matches a ClientEventDataType enum value
        @JsonTypeName("Write")
        data class WriteData(
            val operation: EventOperation,
            val schema: Schema,
            val objectId: ObjectId?,
            val isDraft: Boolean? = null,
            val preChangeData: ObjectNode,
            val postChangeData: ObjectNode,
            val typeHint: TypeHint,
        ) : Data() {
            override val type = ClientEventDataType.Write
        }

        // JsonTypeName matches a ClientEventDataType enum value
        @JsonTypeName("JobOfferResponse")
        data class JobOfferResponseData(
            val resourceJobOfferUid: String,
            val response: OfferResponse,
        ) : Data() {
            override val type = ClientEventDataType.JobOfferResponse
        }

        @JsonTypeName("MexWrite")
        data class MexWriteData(
            val packageId: String,
            val contextObjectId: ObjectId,
            val preInstanceData: JsonObject,
            val postInstanceData: JsonObject
        ) : Data() {
            override val type = ClientEventDataType.MexWrite
        }

        @JsonTypeName("MalFormed")
        data class MalFormedData(
            val causeFields: String? = null,
            val detailedMessage: String? = null
        ) : Data() {
            override val type = ClientEventDataType.MalFormed
        }
    }
}


enum class ClientEventDataType {
    Write,
    JobOfferResponse,
    MexWrite,
    MalFormed
}
enum class OfferResponse {
    Accept,
    Decline,
}
sealed class LockResult<T>(val lockId: String) {
    data class Success<T>(val id: String, val result: T) : LockResult<T>(lockId = id)
    class Locked<T>(val id: String) : LockResult<T>(lockId = id)
    class Failure<T>(val id: String) : LockResult<T>(lockId = id)
}
enum class ShouldContinue {
    CONTINUE,
    STOP
}

data class SequenceWithCas(
    val cas: Long? = null,
    val lastProcessedClientSequence: LastProcessedClientSequence
)
data class LastProcessedClientSequence(
    val id: String,
    val sequence: Long,
    val timeWritten: Instant,
    val deviceId: String = "device-id",
    val sessionId: String? = "session-id",
    val timeProcessed: Instant = Instant.now()
)

sealed class EventOperation(val name: String) {
    object Created : EventOperation("Created")
    object Updated : EventOperation("Updated")
    object Deleted : EventOperation("Deleted")
    object Replaced : EventOperation("Replaced")

    companion object {
        @JsonCreator
        @JvmStatic
        fun findBySimpleClassName(simpleName: String): EventOperation? {
            return EventOperation::class.sealedSubclasses.first {
                it.simpleName == simpleName
            }.objectInstance
        }

        fun findByName(name: String): EventOperation? {
            return EventOperation::class.sealedSubclasses.firstOrNull {
                it.objectInstance?.name == name
            }?.objectInstance
        }
    }
}

sealed class Schema(val name: String) {
    object Accounts : Schema("Accounts")
    object Activities : Schema("Activities")
    object Availabilities : Schema("Availabilities")
    object Contacts : Schema("Contacts")
    object JobAllocations : Schema("JobAllocations")
    object JobTasks : Schema("JobTasks")
    object Jobs : Schema("Jobs")
    object JobStatus : Schema("JobStatus")
    object Resources : Schema("Resources")

    // MobileDeviceSettings does not have an aggregate, but is used to make changes to other
    // aggregates, such as the Resources aggregate. Events marked with this schema will result in modifications
    // made to the Resources Aggregate, which will be pushed to ElasticServer.
    object MobileDeviceSettings : Schema("MobileDeviceSettings")

    object FileMetadata : Schema("FileMetadata")

    class Custom(name: String) : Schema(name)

    companion object {

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        @JvmStatic
        fun findBySimpleClassName(simpleName: String): Schema? {
            return Schema::class.sealedSubclasses.first {
                it.simpleName == simpleName
            }.objectInstance
        }

        fun findByName(name: String): Schema? {
            return Schema::class.sealedSubclasses.firstOrNull {
                it.objectInstance?.name == name
            }?.objectInstance
        }

        fun findByNameOption(name: String): Option<Schema> {
            return Schema::class.sealedSubclasses
                .firstOrNone { it.objectInstance?.name == name }
                .mapNotNull { it.objectInstance }
        }
    }
}

sealed class TypeHint {
    object Activity : TypeHint()
    object Availability : TypeHint()
    object Job : TypeHint()
    object JobAllocation : TypeHint()
    object JobTask : TypeHint()
    object PickList : TypeHint()
    object String : TypeHint()
    object Url : TypeHint()
    object FileMetadata : TypeHint()

    data class Custom(val name: String) : TypeHint()

    companion object {
        @JsonCreator
        @JvmStatic
        fun findBySimpleClassName(simpleName: kotlin.String): TypeHint? {
            return TypeHint::class.sealedSubclasses.first {
                it.simpleName == simpleName
            }.objectInstance
        }
    }
}

data class UserMetadata(
    val id: String
)

data class Job(
    override val globalSequenceNumber: Int?,
    override val id: CBID,
) : ModelWithGlobalSequence<Job> {
    override val type = Job.type
    override fun copyWithGlobalSequence(seq: Int?): Job {
        return this.copy(globalSequenceNumber = seq)
    }
    companion object {
        const val type = "Job"
    }
}

interface ModelWithGlobalSequence<T> : ModelForCouchbase {
    val globalSequenceNumber: Int?

    /** Returns a copy of this model with the [globalSequenceNumber] updated to the specified [seq]. */
    fun copyWithGlobalSequence(seq: Int?): T
}

interface ModelForCouchbase : Identity {
    override val id: CBID

    val type: String
}
interface ID

/**
 * To declare that a model can be uniquely identified
 */
interface Identity {
    val id: ID
}

data class CBID private constructor(val value: String) : ID {

    // Used to avoid having to know the internal representation, but use a string as the couchbase key
    override fun toString(): String {
        return value
    }
    companion object {
        fun job(
            tenantId: TenantId,
            resourceId: ResourceId,
        ): CBID {
            return build(tenantId.value, resourceId.value)
        }

        fun adhoc(
            id: String
        ): CBID {
            return CBID(id)
        }
        private fun build(vararg values: String): CBID {
            return CBID(values.asList().joinToString("|"))
                .also { id ->
                    if (id.value.toByteArray(StandardCharsets.UTF_8).size > 250) {
                        throw RuntimeException("Cannot have couchbase key greater than 250 bytes - $id is too long")
                    }
                }
        }
    }
}
