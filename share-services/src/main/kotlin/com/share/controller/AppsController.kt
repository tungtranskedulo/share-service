package com.share.controller

import com.share.domain.refresh.services.DataRefreshService
import com.share.http.ApiResponse
import com.share.http.CustomAPIRequest
import com.share.http.EmptyDataRequest
import com.share.http.PublicAPIData
import com.share.http.api.ApiSuccessResult
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@RestController
@RequestMapping("/share-services")
class AppsController(
    private val dataRefreshService: DataRefreshService
) {
    @GetMapping("/ping")
    fun healthCheck() = "Pong"

    @GetMapping("/refresh")
    fun refresh() : ApiResponse<RefreshResponse, CustomAPIRequest<EmptyDataRequest>> {
        dataRefreshService.refreshData()

        return ApiResponse.ok(CustomAPIRequest(), RefreshResponse())
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
            //.baseUrl("http://localhost:3000")
            .baseUrl("https://dev-api.test.skl.io")
            .defaultCookie("cookie-name", "cookie-value")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
        val request = mockoonApi.get().uri("/form/bundle/engine/$appVersion/$platform")

        return request
            .retrieve()
            .awaitBody<ApiSuccessResult<String>>()
            .result
    }

    data class RefreshResponse (
        val errors: List<String> = listOf()
    ): PublicAPIData
}
