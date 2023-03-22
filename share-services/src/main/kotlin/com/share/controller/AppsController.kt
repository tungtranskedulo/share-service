package com.share.controller

import com.share.domain.refresh.services.DataRefreshService
import com.share.http.ApiResponse
import com.share.http.CustomAPIRequest
import com.share.http.EmptyDataRequest
import com.share.http.PublicAPIData
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
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

    data class RefreshResponse (
        val errors: List<String> = listOf()
    ): PublicAPIData
}
