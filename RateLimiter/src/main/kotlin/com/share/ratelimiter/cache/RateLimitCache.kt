package com.share.ratelimiter.cache

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import java.util.concurrent.TimeUnit

class RateLimitCache {
    val cache: LoadingCache<String, Int> = CacheBuilder.newBuilder()
        .expireAfterWrite(6, TimeUnit.MINUTES)
        .maximumSize(100)
        .build(CacheLoader.from { _ -> getResource() })

    private fun getResource(): Int {
        return 0
    }

    private fun execute(key: String): Int {
        val count = cache.get(key)
        cache.put(key, count + 1)
        return cache.get(key)
    }
}