package com.share.controller

import com.share.ApiClient
import com.share.aspect.CoroutineLogExecutionTime
import com.share.domain.refresh.services.DataRefreshService
import com.share.http.*
import com.share.http.api.ApiSuccessResult
import com.share.model.FileMetadata
import com.share.model.FileMetadataClientModel
import com.share.model.ObjectId
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.*



private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/share-services")
class AppsController(
    private val dataRefreshService: DataRefreshService,
    private val apiClient: ApiClient,
) {
    @GetMapping("/ping")
    fun healthCheck() = "Pong"

    @GetMapping("/refresh")
    suspend fun refresh(): ApiResponse<RefreshResponse, CustomAPIRequest<EmptyDataRequest>> {
        dataRefreshService.refreshData()

        return ApiResponse.ok()
    }

    @GetMapping("/refresh-entity")
    suspend fun refreshEntity(): ApiSuccessResult<RefreshEntityResponse> {
        return withTenantUserToken {
            dataRefreshService.refreshEntity()?.let {
                ApiSuccessResult(RefreshEntityResponse(it.id.toString(), it.globalSequenceNumber))
            } ?: ApiSuccessResult(RefreshEntityResponse("No entity found", null))
        }
    }

    @GetMapping(value = ["/mexengine/{appVersion}/{platform}"])
    suspend fun fetchMexEngineData(
        @PathVariable("appVersion") appVersion: String,
        @PathVariable("platform") platform: String,
    ): ApiSuccessResult<String> {
        // TODO establish permissions
        return ApiSuccessResult(apiClient.getMexEngine(appVersion, platform))
    }

    @CoroutineLogExecutionTime
    @PutMapping(value = ["/attachments/{parentId}"])
    suspend fun uploadAttachment(
        @PathVariable("parentId") parentId: String,
        request: ServerHttpRequest
    ): ApiSuccessResult<FileMetadataClientModel> {
        return withTenantUserToken {
            val responseResult = apiClient.uploadFile(parentId)
            log.info { "response: $responseResult" }
            ApiSuccessResult(
                responseResult.result
            )

        }
    }


    private suspend fun <T> withTenantUserToken(
        block: suspend () -> T
    ): T {
        return block()
    }

    data class RefreshResponse(
        val type: String = "refresh"
    ) : PublicAPIData

    data class RefreshEntityResponse(val id: String, val globalSequenceNumber: Int?)
}
