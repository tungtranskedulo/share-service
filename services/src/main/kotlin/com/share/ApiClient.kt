package com.share

import com.share.config.JsonObject
import com.share.config.toJson
import com.share.http.RetryConfiguration
import com.share.http.RetryPolicyName
import com.share.http.SkeduloApiClientException
import com.share.http.api.ApiSuccessResult
import com.share.http.retryWithPolicy
import com.share.model.FileMetadata
import com.share.model.FileMetadataClientModel
import com.share.model.ObjectId
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

private val log = KotlinLogging.logger {}

@Component
class ApiClient(
    @Qualifier("mocApiClient")
    private val mocApiClient: WebClient,
    private val retryConfiguration: RetryConfiguration,
    private val condenserApiClient: WebClient,
) {
    suspend fun getMexEngine(
        appVersion: String,
        platform: String,
    ): String {
        val request = mocApiClient.get()
            .uri("/form/bundle/engine/$appVersion/$platform")

        return request
            .retrieve()
            .awaitBody<ApiSuccessResult<String>>()
            .result
    }

    suspend fun fetchCustomFormData(payload: JsonObject): JsonObject {
        val request = condenserApiClient.post().uri("/condenser/form/fetch")

        return request
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(payload.toJson())
            .retrieve()
            .awaitBody()
    }

    suspend fun uploadFile(
        parentId: String,
    ): ApiSuccessResult<FileMetadataClientModel> {
        return mocApiClient.put()
            .uri(
                "/files/attachments/${parentId}"
            )
            .retrieve()
            .awaitBody()
    }

    suspend fun saveCustomFormData(formRevId: String, saveObject: JsonObject): JsonObject {
        val retryPolicy = retryConfiguration.retryPolicyConfiguration()[RetryPolicyName.Condenser]!!

        return retryWithPolicy(retryPolicy) {
            log.info { "Saving form $formRevId" }
            mocApiClient.post().uri("/condenser/form/save")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(CondenserSavePayLoad(formRevId, saveObject).toJson())
                .retrieve()
                .awaitBody()
        }
    }



    suspend fun findFileMetadata(tempId: ObjectId): FileMetadata? {
        return null
//        return FileMetadata(
//            tempId = tempId,
//            uid = ObjectId("file-uid"),
//        )
    }

}

data class CondenserSavePayLoad(val formId: String, val saveObj: JsonObject)
