package com.share.services.graphql

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.*
import java.util.*

class GraphQLMutationFactoryTest : FunSpec({

    val START = "2023-02-25T14:28:11.717Z"
    val FINISH = "2023-02-26T14:28:11.717Z"

    context("Event model to GraphQL model") {
        val mutationFactory = GraphQLMutationFactory()

        test("Should map standard fields - update") {
            val mutation = mutationFactory.toUpdateMutation(
                objectType = Schema.JobAllocations,
                id = ObjectId("a0H2w00000GFIECEA5"),
                eventSchemaVersion = 1,
                eventModel = mapOf(
                    "jobId" to "a0O2w000005CITdEAO",
                    "status" to JobAllocationStatus.CONFIRMED.name,
                    "timeResponded" to "2022-05-19T02:17:06.573Z",
                    "nullValue" to null
                )
            )
            mutation.mutationName shouldBe "updateJobAllocations"
            mutation.schemaName shouldBe "JobAllocations"
            mutation.inputType shouldBe "UpdateJobAllocations"
            @Suppress("UNCHECKED_CAST")
            mutation.keysAndValues as Map<String, Any?> shouldContainExactly mapOf(
                "UID" to "a0H2w00000GFIECEA5",
                "JobId" to "a0O2w000005CITdEAO",
                "Status" to JobAllocationStatus.CONFIRMED.backendName,
                "NullValue" to null,
                "TimeResponded" to "2022-05-19T02:17:06.573Z"
            )
        }

        test("should map create availability payload correctly") {
            val resourceId = UUID.randomUUID().toString()
            val mutation = mutationFactory.toInsertMutation(
                objectType = Schema.Availabilities,
                eventSchemaVersion = 1,
                eventModel = mapOf(
                   // "UID" to "a0H2w00000GFIECEA5",
                    "resourceId" to resourceId,
                    "start" to START,
                    "finish" to FINISH,
                    "status" to "PENDING",
                    "isAvailable" to true,
                    "availabilityType" to "test"
                )
            )
            mutation.mutationName shouldBe "insertAvailabilities"
            mutation.schemaName shouldBe "Availabilities"
            mutation.inputType shouldBe "NewAvailabilities"
            @Suppress("UNCHECKED_CAST")
            mutation.keysAndValues as Map<String, Any?> shouldContainExactly mapOf(
                "ResourceId" to resourceId,
                "Start" to START,
                "Finish" to FINISH,
                "Status" to "Pending",
                "IsAvailable" to true,
                "Type" to "test"
            )
        }
    }
})
