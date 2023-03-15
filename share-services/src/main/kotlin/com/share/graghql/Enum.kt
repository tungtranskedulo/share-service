package com.share.graghql

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.annotation.JsonProperty

enum class AvailabilityStatus(val backendName: String) {

    /*
     * JsonProperty annotation value (eg "EN_ROUTE") is used for persistence in the Edge db (ie
     * couchbase db), eg for aggregates and projections.
     * backendName is used for persistence in the backend (Elastic Server) and GraphQL queries and
     * responses.
     */

    @JsonProperty("PENDING")
    PENDING("Pending"),

    @JsonProperty("APPROVED")
    APPROVED("Approved"),

    @JsonProperty("DECLINED")
    DECLINED("Declined");

    companion object {
    }

    fun toBackendName(): String {
        return backendName
    }
}

enum class JobAllocationStatus(val backendName: String) {

    /*
     * JsonProperty annotation value (eg "EN_ROUTE") is used for persistence in the Edge db (ie
     * couchbase db), eg for aggregates and projections.
     * backendName is used for persistence in the backend (Elastic Server) and GraphQL queries and
     * responses.
     */

    @JsonProperty("PENDING_DISPATCH")
    PENDING_DISPATCH("Pending Dispatch"),

    @JsonProperty("DISPATCHED")
    DISPATCHED("Dispatched"),

    @JsonProperty("CONFIRMED")
    CONFIRMED("Confirmed"),

    @JsonProperty("EN_ROUTE")
    EN_ROUTE("En Route"),

    @JsonProperty("CHECKED_IN")
    CHECKED_IN("Checked In"),

    @JsonProperty("IN_PROGRESS")
    IN_PROGRESS("In Progress"),

    @JsonProperty("COMPLETE")
    COMPLETE("Complete"),

    @JsonProperty("DELETED")
    DELETED("Deleted"),

    @JsonProperty("DECLINED")
    DECLINED("Declined");

    companion object {

        fun fromBackendName(text: String): Either<JobAllocationStatusError, JobAllocationStatus> {
            return values().firstOrNull { it.backendName == text }?.right()
                ?: JobAllocationStatusError.InvalidJobAllocationStatus(IllegalArgumentException(text)).left()
        }
    }

    fun toBackendName(): String {
        return backendName
    }
}


sealed class JobAllocationStatusError {
    data class InvalidJobAllocationStatus(val ex: IllegalArgumentException) : JobAllocationStatusError()
}

enum class JobStatus(val backendName: String) {

    /*
     * JsonProperty annotation value (eg "EN_ROUTE") is used for persistence in the Edge db (ie
     * couchbase db), eg for aggregates and projections.
     * backendName is used for persistence in the backend (Elastic Server) and GraphQL queries and
     * responses.
     */

    @JsonProperty("QUEUED")
    QUEUED("Queued"),

    @JsonProperty("PENDING_ALLOCATION")
    PENDING_ALLOCATION("Pending Allocation"),

    @JsonProperty("PENDING_DISPATCH")
    PENDING_DISPATCH("Pending Dispatch"),

    @JsonProperty("DISPATCHED")
    DISPATCHED("Dispatched"),

    @JsonProperty("READY")
    READY("Ready"),

    @JsonProperty("EN_ROUTE")
    EN_ROUTE("En Route"),

    @JsonProperty("ON_SITE")
    ON_SITE("On Site"),

    @JsonProperty("IN_PROGRESS")
    IN_PROGRESS("In Progress"),

    @JsonProperty("COMPLETE")
    COMPLETE("Complete"),

    @JsonProperty("CANCELLED")
    CANCELLED("Cancelled");

    companion object {

        fun fromBackendName(text: String): Either<JobStatusError, JobStatus> {
            return values().firstOrNull { it.backendName == text }?.right()
                ?: JobStatusError.InvalidJobStatus(IllegalArgumentException(text)).left()
        }
    }

    fun toBackendName(): String {
        return backendName
    }
}


sealed class JobStatusError {
    data class InvalidJobStatus(val ex: IllegalArgumentException) : JobStatusError()
}

enum class NotificationType(val backendName: String) {

    /*
     * JsonProperty annotation value (eg "PUSH") is used for persistence in the Edge db (ie
     * couchbase db), eg for aggregates and projections.
     * backendName is used for persistence in the backend (Elastic Server) and GraphQL queries and
     * responses.
     */

    @JsonProperty("SMS")
    SMS("sms"),

    @JsonProperty("PUSH")
    PUSH("push");

    companion object {

        fun fromBackendName(text: String): Either<NotificationTypeError, NotificationType> {
            return values().firstOrNull { it.backendName == text }?.right()
                ?: NotificationTypeError.InvalidNotificationType(IllegalArgumentException(text)).left()
        }
    }

    fun toBackendName(): String {
        return backendName
    }
}


sealed class NotificationTypeError {
    data class InvalidNotificationType(val ex: IllegalArgumentException) : NotificationTypeError()
}
