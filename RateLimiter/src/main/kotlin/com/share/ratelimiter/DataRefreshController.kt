package com.share.ratelimiter

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DataRefreshController {
    @PostMapping(value = ["/refresh"])
    suspend fun refreshOrgAndUserData() {

    }
}