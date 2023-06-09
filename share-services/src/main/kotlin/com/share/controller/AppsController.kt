package com.share.controller

import com.share.domain.refresh.services.DataRefreshService
import com.share.http.*
import com.share.http.api.ApiSuccessResult
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/share-services")
class AppsController(
    private val dataRefreshService: DataRefreshService
) {
    @GetMapping("/ping")
    fun healthCheck() = "Pong"

    @GetMapping("/refresh")
    suspend fun refresh() : ApiResponse<RefreshResponse, CustomAPIRequest<EmptyDataRequest>> {
        dataRefreshService.refreshData()

        return ApiResponse.ok()
    }

    @GetMapping("/refresh-entity")
    suspend fun refreshEntity() : ApiSuccessResult<RefreshEntityResponse>  {
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
        return ApiSuccessResult(getMexEngine(appVersion, platform))
    }

    suspend fun getMexEngine(
        appVersion: String,
        platform: String,
    ): String {
        val mockoonApi = WebClient.builder()
            .baseUrl("http://localhost:3000")
//            .baseUrl("https://dev-api.test.skl.io")
            .defaultCookie("cookie-name", "cookie-value")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
        val request = mockoonApi.get().uri("/form/bundle/engine/$appVersion/$platform")

        return request
            .retrieve()
            .awaitBody()
    }

    @PutMapping(value = ["/attachments/{parentId}"])
    suspend fun uploadFile(
        @PathVariable("parentId") parentId: String,
        request: HttpServletRequest,
    ): ApiSuccessResult<String> {
        return withTenantUserToken {
            ApiSuccessResult(
                result = getMexEngine(parentId, "android")
            )
        }
    }

    private suspend fun <T> withTenantUserToken(
        block: suspend () -> T
    ): T {
        return block()
    }

    data class RefreshResponse (
        val type: String = "refresh"
    ): PublicAPIData

    data class RefreshEntityResponse(val id: String, val globalSequenceNumber: Int?)
}
