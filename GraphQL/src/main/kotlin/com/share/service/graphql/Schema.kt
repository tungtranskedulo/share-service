package com.share.service.graphql

import com.fasterxml.jackson.annotation.JsonCreator
import arrow.core.Option
import arrow.core.firstOrNone
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import java.rmi.server.UID
import java.time.Instant

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