package com.share.cache

//import com.couchbase.client.java.codec.Transcoder
import org.springframework.lang.Nullable
import java.time.Duration

interface BasedCacheWriter {
    suspend fun put(
        key: String,
        value: Any,
        @Nullable expiry: Duration? = null,
        //@Nullable transcoder: Transcoder? = null
    ) : Any?

    suspend fun get(key: String): Any?
}

enum class CacheNames {
    TenantConfiguration,
    TenantData,
    TenantActivity
}

enum class KeyGenerator {
    CacheKeyWithTenantIdGeneratorFromToken,
    CacheKeyWithTenantIdGeneratorFromFirstParam
}
