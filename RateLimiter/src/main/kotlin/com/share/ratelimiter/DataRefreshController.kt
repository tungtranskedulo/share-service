package com.share.ratelimiter

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DataRefreshController {
    @GetMapping("/ping")
    fun healthCheck() = "Pong"
}