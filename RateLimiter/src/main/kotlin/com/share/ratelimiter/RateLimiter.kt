package com.share.ratelimiter

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalTime

@Service
class RateLimiter(
    @Value("\${rateLimiter.resource}")
    private val vidaModuleEnabled: List<Int>,
) {
    fun isAllow(
        resourceId:String
    ): Boolean {
        val currentWindow = getCurrentFixedWindow()
        val key = java.lang.String.format("resource_%s:%s", resourceId, currentWindow)
        return true
    }

    fun getCurrentFixedWindow(): Int {
        val currentMinute: Int = LocalTime.now().minute
        return currentMinute / 6
    }
}
