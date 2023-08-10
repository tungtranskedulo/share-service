package com.share.cache

//import com.couchbase.client.core.error.DocumentExistsException
//import com.couchbase.client.core.error.DocumentNotFoundException
//import com.couchbase.client.java.codec.Transcoder
//import com.couchbase.client.java.kv.GetOptions
//import com.couchbase.client.java.kv.UpsertOptions
//import com.skedulo.services.mobileedge.config.ReactiveCouchbaseConnection
import kotlinx.coroutines.reactive.awaitFirstOrNull
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Duration

private val log = KotlinLogging.logger {}

data class CachedValue<V>(
    val value: V,
    val isRetrieved: Boolean, // true if the value is retrieved from the cache
)

@Component
class CouchbaseCacheWriter : BasedCacheWriter {

//    @Autowired
//    private lateinit var cacheCbConnection: ReactiveCouchbaseConnection

    private val cacheConfiguration = mapOf(
        CacheNames.TenantData to CouchbaseCacheConfiguration.defaultCacheConfig(
            Duration.ofSeconds(30)
        )
    )

    @Suppress("UNCHECKED_CAST")
    suspend fun <V> runCachedBy(
        cacheNames: CacheNames,
        keyGenerator: KeyGenerator,
        key: String,
        forceRefresh: Boolean = false,
        onCacheExceptionBlock: ((Exception) -> Exception)? = null,
        block: suspend () -> V?,
    ): CachedValue<V> {
        try {
            val cachedValue = if (forceRefresh) null else get(key)
            if (cachedValue != null) {
                return CachedValue(cachedValue as V, true)
            }
            return CachedValue(block()?.also {
                put(key, it, cacheConfiguration[cacheNames]?.expiry)
            } ?: null as V, false)
        } catch (e: Exception) {
            onCacheExceptionBlock?.invoke(e)
            return CachedValue(block() ?: null as V, false)
        }
    }

    override suspend fun get(key: String): Any? {
//        return try {
//            val found = cacheCbConnection.collection.get(
//                key,
//                GetOptions.getOptions().transcoder(transcoder)
//            ).awaitFirstOrNull()
//            found?.contentAs(Any::class.java)
//        } catch (e: DocumentNotFoundException) {
//            null
//        }
        return mockGetValue()
    }


    override suspend fun put(
        key: String, value: Any, expiry: Duration?
    ): Any? {
//        return try {
//            val options = UpsertOptions.upsertOptions()
//            if (expiry != null) {
//                options.expiry(expiry)
//            }
//            if (transcoder != null) {
//                options.transcoder(transcoder)
//            }
//
//            cacheCbConnection.collection.upsert(key, value, options).awaitFirstOrNull()
//            value
//        } catch (ex: DocumentExistsException) {
//            null
//        }
        return mockPutValue()
    }

    private fun mockGetValue(): Any? {
        return null
    }

    private fun mockPutValue(): Any? {
        return null
    }


}
