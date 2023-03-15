package com.share.controller

import com.share.domain.refresh.services.DataRefreshService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AppsController(
    private val dataRefreshService: DataRefreshService
) {
    @GetMapping("/ping")
    fun healthCheck() = "Pong"

    @GetMapping("/refreshdata")
    fun refresh() {
        dataRefreshService.refreshData()
    }
}
