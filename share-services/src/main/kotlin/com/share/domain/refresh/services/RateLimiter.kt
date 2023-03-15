package com.share.domain.refresh.services

import mu.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import java.time.LocalTime

private val log = KotlinLogging.logger {}
@Service
class RateLimiter(
) {
    fun isAllow(
        resourceId: String
    ): Boolean {
        val currentWindow = getCurrentFixedWindow()
        val key = java.lang.String.format("resource_%s:%s", resourceId, currentWindow)
        log.info { "key: $key" }
        return true
    }

    fun getCurrentFixedWindow(): Int {
        val currentMinute: Int = LocalTime.now().minute
        return currentMinute / 6
    }

}

@ConstructorBinding
@ConfigurationProperties("skedulo.app.rate-limiter.resources")
data class RateLimitConfig(
    val maxRequest: Int = 1,
    val timeWindowMinute: Int = 6
)

