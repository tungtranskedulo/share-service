package com.share.domain.refresh.services

import com.share.ApiClient
import com.share.aspect.CoroutineLogExecutionTime
import com.share.model.CBID
import com.share.model.Identity
import com.share.model.TenantId
import mu.KotlinLogging
import org.checkerframework.checker.units.qual.g
import org.springframework.stereotype.Service
import java.lang.reflect.Method
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor

private val log = KotlinLogging.logger {}

//interface BaseCacheEntity : Identity {
//    override val id: CBID
//    val value: Any?
//}

interface BaseCacheEntity {
    val id: String
    val value: Any?
}
data class CustomFieldCacheEntity(
    override val id: String,
    override val value: List<CustomFieldDefinitionClientModel> ? = null,
) : BaseCacheEntity

data class CustomFieldDefinitionClientModel(
    val id: String,
    val name: String
)

@Service
class CacheService(
    private val customFormService: CustomFormService
) {
    @CoroutineLogExecutionTime
    suspend fun get(): Map<String, Any> {
        return runCachedBy(CustomFieldDefinitionClientModel::class,"123") {
            customFormService.getCustomForm2("a", 2)
        } as Map<String, Any>
    }
}

suspend fun <T> runCachedBy(
    clazz: KClass<*>,
    vararg params: Any? = emptyArray(),
    onCacheExceptionBlock: ((Exception) -> Exception)? = null,
    block: suspend () -> T,
): T? {
    try {
        log.info { "$params - $clazz" }
        val constructor = CustomFieldDefinitionClientModel::class.primaryConstructor
        val instance = constructor!!.call("Bob", 23)
        log.info { "$instance" }
    } catch (e: Exception) {
        log.error { e }
        onCacheExceptionBlock?.invoke(e)
        return block()
    }

    return block()?.also {
        log.info { it }
    }
}

fun <T: Any> createInstance(clazz: KClass<T>, vararg args: Any): T {
    val constructor = clazz.java.getConstructor(*args.map { it::class.java }.toTypedArray()) // gets the constructor with the given parameter types
    return constructor.newInstance(*args) // creates an instance with the given arguments
}


private fun <V> generateKeyWithTenantIdGeneratorFromFirstParam(
    params: Array<out Any?>,
    block: suspend () -> V,
): String {
    val key = StringBuilder()
    if (params.isNotEmpty() && params[0] is TenantId) {
        key.append((params[0] as TenantId).value)
            .append("|").append(block::class.java.name.substringAfterLast("."))
        if (params.size > 1) {
            key.append("|").append(params.sliceArray(1 until params.size).joinToString("_"))
        }
        return key.toString()

    } else throw Exception("TenantIdParamCacheKeyGenerator requires a TenantId as the first parameter")
}

data class CachedEntity(
    val value: Any
)
