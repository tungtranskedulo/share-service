package com.share.graghql

import com.share.graghql.*
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties


class SchemaBuilder(val function: (Map<String, Any?>) -> Map<String, Any?>) {
    operator fun invoke(eventModel: Map<String, Any?>) = function(eventModel)
}

object GraphQLSchemaBuilder {

    private const val UID = "UID"
    private const val ResourceId = "ResourceId"
    private const val Start = "Start"
    private const val Finish = "Finish"
    private const val Status = "Status"
    private const val Job_Status = "JobStatus"
    private const val Name = "Name"
    private const val Description = "Description"
    private const val Type = "Type"
    private const val Notes = "Notes"
    private const val Notification_Type = "NotificationType"
    private const val AvailabilityType  = "AvailabilityType"
    private const val CustomFields = "CustomFields"

    @Suppress("UNCHECKED_CAST")
    fun buildDefaultEventModel(
        eventModel: Map<String, Any?>,
    ): Map<String, Any?> {
        return eventModel.toMutableMap().also {
            if (it.containsKey(Notification_Type)) {
                it[Notification_Type] = NotificationType.valueOf(it[Notification_Type] as String).toBackendName()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun buildAvailability(
        eventModel: Map<String, Any?>,
    ): Map<String, Any?> {
        val map = eventModel.toMutableMap().also {
            if (it.containsKey(AvailabilityType)) {
                it[Type] = it[AvailabilityType]
                it.remove(AvailabilityType)
            }
        }
        val model = with(map) {
            EventModel.AvailabilitiesEventModel(
                UID = this[UID] as String?,
                ResourceId = this[ResourceId] as String?,
                Start = this[Start] as String?,
                Finish = this[Finish] as String?,
                Status = (this[Status] as String?)?.let { AvailabilityStatus.valueOf(it).toBackendName() },
                IsAvailable = this["IsAvailable"] as Boolean?,
                Type = this[Type] as String?,
                Notes = this[Notes] as String?,
                CustomFields = this[CustomFields] as Map<String, Any?>?,
            )
        }
        return model.flattenObjToMap().filterMappedKeys(map)
    }

    @Suppress("UNCHECKED_CAST")
    fun buildJob(
        eventModel: Map<String, Any?>,
    ): Map<String, Any?> {
        val map = eventModel.toMutableMap().also {
            if (it.containsKey(Status)) {
                it[Job_Status] = it[Status]
                it.remove(Status)
            }
        }
        val model = with(eventModel) {
            EventModel.JobEventModel(
                UID = this[UID] as String?,
                AbortReason = this["AbortReason"] as String?,
                AccountId = this["AccountId"] as String?,
                ActualEnd = this["ActualEnd"] as String?,
                ActualStart = this["ActualStart"] as String?,
                Address = this["Address"] as String?,
                AutoSchedule = this["AutoSchedule"] as Boolean?,
                CanBeDeclined = this["CanBeDeclined"] as Boolean?,
                CompletionNotes = this["CompletionNotes"] as String?,
                ContactId = this["ContactId"] as String?,
                CopiedFromId = this["CopiedFromId"] as String?,
                CustomerConfirmationStatus = this["CustomerConfirmationStatus"] as String?,
                Description = this[Description] as String?,
                Duration = this["Duration"] as Int?,
                End = this["End"] as String?,
                EstimatedEnd = this["EstimatedEnd"] as String?,
                EstimatedStart = this["EstimatedStart"] as String?,
                FollowupReason = this["FollowupReason"] as String?,
                GeoLatitude = this["GeoLatitude"] as Double?,
                GeoLongitude = this["GeoLongitude"] as Double?,
                IsGroupEvent = this["IsGroupEvent"] as Boolean?,
                JobAllocationTimeSource = this["JobAllocationTimeSource"] as Boolean?,
                JobStatus = (this[Job_Status] as String?)?.let {
                    JobStatus.valueOf(this[Job_Status] as String).toBackendName()
                },
                LocationId = this["LocationId"] as String?,
                Locked = this["Locked"] as Boolean?,
                MaxAttendees = this["MaxAttendees"] as Int?,
                MinAttendees = this["MinAttendees"] as Int?,
                Name = this[Name] as String?,
                NotesComments = this["NotesComments"] as String?,
                NotifyBy = this["NotifyBy"] as String?,
                NotifyPeriod = this["NotifyPeriod"] as Int?,
                ParentId = this["ParentId"] as String?,
                Quantity = this["Quantity"] as Int?,
                RecurringScheduleId = this["RecurringScheduleId"] as String?,
                RegionId = this["RegionId"] as String?,
                ScheduleTemplateId = this["ScheduleTemplateId"] as String?,
                Start = this[Start] as String?,
                TemplatedJobId = this["TemplatedJobId"] as String?,
                Type = this[Type] as String?,
                Urgency = this["Urgency"] as String?,
                VirtualMeetingId = this["VirtualMeetingId"] as String?,
                VirtualMeetingInfo = this["VirtualMeetingInfo"] as String?,
                VirtualMeetingURL = this["VirtualMeetingURL"] as String?,
                CustomFields = this[CustomFields] as Map<String, Any?>?,
            )
        }

        return model.flattenObjToMap().filterMappedKeys(map)
    }

    @Suppress("UNCHECKED_CAST")
    fun buildJobAllocation(
        eventModel: Map<String, Any?>,
    ): Map<String, Any?> {
        val model = with(eventModel) {
            EventModel.JobAllocationsEventModel(
                UID = this[UID] as String?,
                DeclineDescription = this["DeclineDescription"] as String?,
                DeclineReason = this["DeclineReason"] as String?,
                Duration = this["Duration"] as Int?,
                End = this["End"] as String?,
                EstimatedTravelDistance = this["EstimatedTravelDistance"] as Double?,
                EstimatedTravelTime = this["EstimatedTravelTime"] as Int?,
                GeoCheckedInLatitude = this["GeoCheckedInLatitude"] as Double?,
                GeoCheckedInLongitude = this["GeoCheckedInLongitude"] as Double?,
                GeoCompletedLatitude = this["GeoCompletedLatitude"] as Double?,
                GeoCompletedLongitude = this["GeoCompletedLongitude"] as Double?,
                GeoInProgressLatitude = this["GeoInProgressLatitude"] as Double?,
                GeoInProgressLongitude = this["GeoInProgressLongitude"] as Double?,
                GeoStartTravelLatitude = this["GeoStartTravelLatitude"] as Double?,
                GeoStartTravelLongitude = this["GeoStartTravelLongitude"] as Double?,
                JobId = this["JobId"] as String?,
                NotificationType = (this[Notification_Type] as String?)?.let {
                    NotificationType.valueOf(it).toBackendName()
                },
                PhoneResponse = this["PhoneResponse"] as String?,
                ResourceId = this[ResourceId] as String?,
                ResourceRequirementId = this["ResourceRequirementId"] as String?,
                Start = this[Start] as String?,
                Status = (this[Status] as String?)?.let {
                    JobAllocationStatus.valueOf(this[Status] as String).toBackendName()
                },
                TeamLeader = this["TeamLeader"] as Boolean?,
                TimeCheckedIn = this["TimeCheckedIn"] as String?,
                TimeCompleted = this["TimeCompleted"] as String?,
                TimeInProgress = this["TimeInProgress"] as String?,
                TimePublished = this["TimePublished"] as String?,
                TimeResponded = this["TimeResponded"] as String?,
                TimeStartTravel = this["TimeStartTravel"] as String?,
                TravelDistance = this["TravelDistance"] as Double?,
                TravelTime = this["TravelTime"] as Int?,
                CustomFields = this[CustomFields] as Map<String, Any?>?,
            )
        }
        return model.flattenObjToMap().filterMappedKeys(eventModel)
    }

    @Suppress("UNCHECKED_CAST")
    fun buildAccount(
        eventModel: Map<String, Any?>,
    ): Map<String, Any?> {
        val model = with(eventModel) {
            EventModel.AccountsEventModel(
                UID = this[UID] as String?,
                BillingCity = this["BillingCity"] as String?,
                BillingPostalCode = this["BillingPostalCode"] as String?,
                BillingState = this["BillingState"] as String?,
                BillingStreet = this["BillingStreet"] as String?,
                Fax = this["Fax"] as String?,
                Name = this[Name] as String?,
                Phone = this["Phone"] as String?,
                Rank = this["Rank"] as Int?,
                RequiresWhitelist = this["RequiresWhitelist"] as Boolean?,
                ShippingCity = this["ShippingCity"] as String?,
                ShippingPostalCode = this["ShippingPostalCode"] as String?,
                ShippingState = this["ShippingState"] as String?,
                ShippingStreet = this["ShippingStreet"] as String?,
                CustomFields = this[CustomFields] as Map<String, Any?>?,
            )
        }
        return model.flattenObjToMap().filterMappedKeys(eventModel)
    }

    @Suppress("UNCHECKED_CAST")
    fun buildActivity(
        eventModel: Map<String, Any?>,
    ): Map<String, Any?> {
        val model = with(eventModel) {
            EventModel.ActivitiesEventModel(
                UID = this[UID] as String?,
                Address = this["Address"] as String?,
                CopiedFromId = this["CopiedFromId"] as String?,
                End = this["End"] as String?,
                GeoLatitude = this["GeoLatitude"] as Double?,
                GeoLongitude = this["GeoLongitude"] as Double?,
                LocationId = this["LocationId"] as String?,
                Notes = this[Notes] as String?,
                Quantity = this["Quantity"] as Int?,
                ResourceId = this["ResourceId"] as String?,
                ScheduleTemplateId = this["ScheduleTemplateId"] as String?,
                Start = this[Start] as String?,
                TemplatedActivityId = this["TemplatedActivityId"] as String?,
                Timezone = this["Timezone"] as String?,
                Type = this[Type] as String?,
                CustomFields = this[CustomFields] as Map<String, Any?>?,
            )
        }
        return model.flattenObjToMap().filterMappedKeys(eventModel)
    }

    @Suppress("UNCHECKED_CAST")
    fun buildContact(
        eventModel: Map<String, Any?>,
    ): Map<String, Any?> {
        val model = with(eventModel) {
            EventModel.ContactsEventModel(
                UID = this[UID] as String?,
                AccountId = this["AccountId"] as String?,
                Email = this["Email"] as String?,
                FirstName = this["FirstName"] as String?,
                LastName = this["LastName"] as String?,
                MailingCity = this["MailingCity"] as String?,
                MailingPostalCode = this["MailingPostalCode"] as String?,
                MailingState = this["MailingState"] as String?,
                MailingStreet = this["MailingStreet"] as String?,
                MobilePhone = this["MobilePhone"] as String?,
                OtherCity = this["OtherCity"] as String?,
                OtherPostalCode = this["OtherPostalCode"] as String?,
                OtherState = this["OtherState"] as String?,
                OtherStreet = this["OtherStreet"] as String?,
                Phone = this["Phone"] as String?,
                RegionId = this["RegionId"] as String?,
                Title = this["Title"] as String?,
                CustomFields = this[CustomFields] as Map<String, Any?>?,
            )
        }
        return model.flattenObjToMap().filterMappedKeys(eventModel)
    }

    @Suppress("UNCHECKED_CAST")
    fun buildJobTask(
        eventModel: Map<String, Any?>,
    ): Map<String, Any?> {
        val model = with(eventModel) {
            EventModel.JobTasksEventModel(
                UID = this[UID] as String?,
                Completed = this["Completed"] as Boolean?,
                Description = this["Description"] as String?,
                JobId = this["JobId"] as String?,
                Name = this[Name] as String?,
                Seq = this["Seq"] as Int?,
                CustomFields = this[CustomFields] as Map<String, Any?>?,
            )
        }
        return model.flattenObjToMap().filterMappedKeys(eventModel)
    }

    @Suppress("UNCHECKED_CAST")
    fun buildResources(
        eventModel: Map<String, Any?>,
    ): Map<String, Any?> {
        val model = with(eventModel) {
            EventModel.ResourcesEventModel(
                UID = this[UID] as String?,
                Alias = this["Alias"] as String?,
                AutoSchedule = this["AutoSchedule"] as Boolean?,
                Category = this["Category"] as String?,
                CountryCode = this["CountryCode"] as String?,
                Email = this["Email"] as String?,
                EmploymentType = this["EmploymentType"] as String?,
                GeoLatitude = this["GeoLatitude"] as Double?,
                GeoLongitude = this["GeoLongitude"] as Double?,
                HomeAddress = this["HomeAddress"] as String?,
                IsActive = this["IsActive"] as Boolean?,
                MobilePhone = this["MobilePhone"] as String?,
                Name = this[Name] as String?,
                Notes = this[Notes] as String?,
                NotificationType = (this[Notification_Type] as String?)?.let {
                    NotificationType.valueOf(it).toBackendName()
                },
                PrimaryPhone = this["PrimaryPhone"] as String?,
                PrimaryRegionId = this["PrimaryRegionId"] as String?,
                Rating = this["Rating"] as Int?,
                ResourceType = this["ResourceType"] as String?,
                UserId = this["UserId"] as String?,
                WeeklyHours = this["WeeklyHours"] as Double?,
                WorkingHourType = this["WorkingHourType"] as String?,
                CustomFields = this[CustomFields] as Map<String, Any?>?,
            )
        }
        return model.flattenObjToMap().filterMappedKeys(eventModel)
    }

    @Suppress("UNCHECKED_CAST")
    private fun Map<String, Any?>.filterMappedKeys(map: Map<String, Any?>): Map<String, Any?> {
        return this.filterKeys { it in map.keys }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> T.flattenObjToMap(): Map<String, Any> {
        return (this::class as KClass<T>).memberProperties.associate { prop ->
            (prop.name to prop.get(this)?.let { value ->
                if (value::class.isData) {
                    value.flattenObjToMap()
                } else {
                    value
                }
            })
        } as Map<String, Any>
    }
}
