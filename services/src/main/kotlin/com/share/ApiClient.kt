package com.share

import com.share.http.api.ApiSuccessResult
import com.share.model.FileMetadata
import com.share.model.FileMetadataClientModel
import com.share.model.ObjectId
import com.share.model.Schema
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Flux


@Component
class ApiClient(
    @Qualifier("mocApiClient")
    private val mocApiClient: WebClient
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

    suspend fun findFileMetadata(tempId: ObjectId): FileMetadata? {
        return null
//        return FileMetadata(
//            tempId = tempId,
//            uid = ObjectId("file-uid"),
//        )
    }

}
