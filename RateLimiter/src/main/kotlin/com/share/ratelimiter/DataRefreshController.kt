package com.share.ratelimiter

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalTime
import java.util.concurrent.TimeUnit

@RestController
class DataRefreshController(
    private val rateLimiter: RateLimiter
) {

    val responseCache: LoadingCache<String, List<String>> = CacheBuilder.newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .maximumSize(100)
        .build(CacheLoader.from { _ -> getResponse() })


    private fun getResponse(): List<String> {
        return emptyList()
    }

    private fun response(key: String, request: String): List<String> {
        val count = responseCache.get(key)
        responseCache.put(key, count.plus(request))
        return responseCache.get(key)
    }


    @PostMapping(value = ["/refresh"])
    fun refreshOrgAndUserData(@RequestParam resourceId: String): Boolean {
        return rateLimiter.isAllow(resourceId)
    }

    private fun fixedWindow(resourceId: String): List<String> {
        val currentWindow = getCurrentFixedWindow()
        val key = java.lang.String.format("resource_%s:%s", resourceId, currentWindow)

        val inc = increase(key)
        return if (inc <= MAX_REQUESTS_PER_MINUTE) {
            response(key, "Data refreshed at ${LocalTime.now()}")
        } else {
            response(key, "Too many refresh ${LocalTime.now()}")
        }
    }
}