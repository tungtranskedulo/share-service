package com.share.domain.refresh.services

import com.share.http.api.ApiSuccessResult
import com.share.logging.CoroutineLogExecutionTime
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.*

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.lang.Thread.sleep
import kotlin.streams.asSequence
import kotlin.streams.toList

private val log = KotlinLogging.logger {}

@Service
class CustomFormService(
    @Value("\${skedulo.app.config.limit-concurrent-coroutine}")
    private val limitConcurrentCoroutine: Int,
) {
    @CoroutineLogExecutionTime
    suspend fun getCustomForm(): String {
        return "1"
    }

    suspend fun getNextEvent(): Either<Error, String?> {
        return null.right()
    }

    @CoroutineLogExecutionTime
    suspend fun fetchAllInstanceDataForJob(): List<MexInstanceData> {
        val fetchData = findAll().pmap {
            getMexFetchData(it.id)
        }

        return fetchData.filterNotNull().toList()
    }

    suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
        val requestSemaphore = Semaphore(limitConcurrentCoroutine)
        map {
            async {
                requestSemaphore.withPermit {
                    f(it)
                }
            }
        }.awaitAll()
    }

    suspend fun findAll(): List<MexInstance> {
        val instances = mutableListOf<MexInstance>()
        for (i in 1..10) {
            instances.add(MexInstance(i.toString(), "name$i"))
        }
        return instances
    }

    suspend fun getMexFetchData(id: String): MexInstanceData? {
        try {
            if (id == "7") {
                throw java.lang.Exception()
            }
            val instance = MexInstanceData(id, getMexEngine(id, "android"))
            log.info { "fetching data for $id, engine: ${instance.engine}" }
            return instance
        } catch (e: Exception) {
            log.error { "Something wrong when processing this FormDef: $id" }
            return null
        }
    }

    suspend fun getMexEngine(
        appVersion: String,
        platform: String,
    ): String {
        val mockoonApi = WebClient.builder()
            .baseUrl("http://localhost:3000")
            .defaultCookie("cookie-name", "cookie-value")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
        val request = mockoonApi.get().uri("/form/bundle/engine/$appVersion/$platform")

        return request
            .retrieve()
            .awaitBody<ApiSuccessResult<String>>()
            .result
    }
}

data class MexInstance(val id: String, val name: String)

data class MexInstanceData(val id: String, val engine: String)
