package com.share.domain.refresh.services

import com.share.aspect.CoroutineLogExecutionTime
import com.share.http.api.ApiSuccessResult
import com.share.model.MexInstance
import com.share.model.MexInstanceData
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import mu.KotlinLogging
import org.openjdk.jmh.annotations.*
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}

@Service
class FetchParallel {
    companion object {
        val limitConcurrentCoroutine = 10
    }

    @CoroutineLogExecutionTime
    suspend fun fetchAllInstanceDataForJob(): List<MexInstanceData> {
        val fetchData = findAll().pmap {
            getMexFetchData(it.id)
        }

        log.info { "fetchData: $fetchData" }
        return fetchData.filterNotNull().toList()
    }

    suspend fun findAll(): List<MexInstance> {
        val instances = mutableListOf<MexInstance>()
        for (i in 1..20) {
            instances.add(MexInstance(i.toString(), "name$i"))
        }
        return instances
    }

    suspend fun getMexFetchData(id: String): MexInstanceData? {
        return try {
            if(id == "10" || id == "5") {
               throw Exception("error")
            }
            val instance = MexInstanceData(id, getMexEngine(id, "android"))
            log.info { "fetching data for $id, engine: ${instance.engine}" }
            instance
        } catch (e: Exception) {
            log.error { "Something wrong when processing this FormDef: $id" }
            null
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

    suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
        val requestSemaphore = Semaphore(limitConcurrentCoroutine)
        map {
            async(Dispatchers.IO) {
                requestSemaphore.withPermit {
                    f(it)
                }
            }
        }.awaitAll()
    }
}
