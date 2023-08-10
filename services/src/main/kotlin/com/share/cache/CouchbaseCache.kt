package com.share.cache

import com.share.model.TenantId
import org.springframework.beans.factory.annotation.Autowired

abstract class CouchbaseCache<T> {

    @Autowired
    protected lateinit var writer: CouchbaseCacheWriter

//    @Autowired
//    protected lateinit var reactiveSecurity: ReactiveSecurity

    abstract suspend fun get(
        persistExecution: suspend (T) -> Any = { },
        forceRefresh: Boolean = false,
        params: Array<out Any?> = emptyArray(),
    ): T

    suspend fun cacheByTenantData(
        block: suspend () -> T?,
        persistExecution: suspend (T) -> Any,
        forceRefresh: Boolean = false,
        vararg params: Any? = emptyArray(),
    ): T {
        return writer.runCachedBy(
            cacheNames = CacheNames.TenantData,
            keyGenerator = KeyGenerator.CacheKeyWithTenantIdGeneratorFromToken,
            key = generateKey(KeyGenerator.CacheKeyWithTenantIdGeneratorFromToken, params, block, persistExecution),
            forceRefresh = forceRefresh
        ) {
            block()
        }.also { result ->
            if (result.value != null && !result.isRetrieved) {
                persistExecution(result.value)
            }
        }.value
    }

    suspend fun cacheByTenantConfiguration(
        block: suspend () -> T?,
        persistExecution: suspend (T) -> Any,
        forceRefresh: Boolean = false,
        params: Array<out Any?> = emptyArray(),
    ): T {
        return writer.runCachedBy(
            cacheNames = CacheNames.TenantConfiguration,
            keyGenerator = KeyGenerator.CacheKeyWithTenantIdGeneratorFromToken,
            key = generateKey(KeyGenerator.CacheKeyWithTenantIdGeneratorFromToken, params, block, persistExecution),
            forceRefresh = forceRefresh
        ) {
            block()
        }.also { result ->
            if (result.value != null && !result.isRetrieved) {
                persistExecution(result.value)
            }
        }.value
    }

    suspend fun cacheByTenantActivity(
        block: suspend () -> T?,
        persistExecution: suspend (T) -> Any,
        forceRefresh: Boolean = false,
        params: Array<out Any?> = emptyArray(),
    ): T {
        return writer.runCachedBy(
            cacheNames = CacheNames.TenantActivity,
            keyGenerator = KeyGenerator.CacheKeyWithTenantIdGeneratorFromFirstParam,
            key = generateKey(
                KeyGenerator.CacheKeyWithTenantIdGeneratorFromFirstParam,
                params,
                block,
                persistExecution
            ),
            forceRefresh = forceRefresh
        ) {
            block()
        }.also { result ->
            if (result.value != null && !result.isRetrieved) {
                persistExecution(result.value)
            }
        }.value
    }


    private fun generateKey(
        keyGenerator: KeyGenerator,
        params: Array<out Any?>,
        block: suspend () -> T?,
        persistExecution: suspend (T) -> Any,
    ): String {
        val blockNames = block::class.java.name.substringAfterLast(".")
            .plus(persistExecution::class.java.name.substringAfterLast("."))
        return when (keyGenerator) {
            KeyGenerator.CacheKeyWithTenantIdGeneratorFromToken -> generateKeyWithTenantIdGeneratorFromToken(
                params,
                blockNames
            )

            KeyGenerator.CacheKeyWithTenantIdGeneratorFromFirstParam -> generateKeyWithTenantIdGeneratorFromFirstParam(
                params, blockNames
            )
        }
    }

    private fun generateKeyWithTenantIdGeneratorFromToken(params: Array<out Any?>, blockNames: String): String {
        val key = StringBuilder()
        key.append("tenantId")
            .append("|").append(blockNames)
        if (params.isNotEmpty()) {
            key.append("|").append(params.joinToString("_"))
        }

        return key.toString()
    }

    private fun generateKeyWithTenantIdGeneratorFromFirstParam(
        params: Array<out Any?>,
        blockNames: String,
    ): String {
        val key = StringBuilder()
        if (params.isNotEmpty() && params[0] is TenantId) {
            key.append((params[0] as TenantId).value)
                .append("|").append(blockNames)
            if (params.size > 1) {
                key.append("|").append(params.sliceArray(1 until params.size).joinToString("_"))
            }
            return key.toString()

        } else throw Exception("TenantIdParamCacheKeyGenerator requires a TenantId as the first parameter")
    }

}
