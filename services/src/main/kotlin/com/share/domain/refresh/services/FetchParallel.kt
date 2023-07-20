package com.share.domain.refresh.services

import com.share.aspect.CoroutineLogExecutionTime
import com.share.common.Util.pmap
import com.share.http.api.ApiSuccessResult
import com.share.model.MexInstance
import com.share.model.MexInstanceData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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

    @OptIn(FlowPreview::class)
    @CoroutineLogExecutionTime
    suspend fun fetchAllInstanceDataForJob(): Flow<MexInstanceData> {
        val chunks = findAll().chunked(10)
        val results = chunks.pmap(maxOf(chunks.size, 1)) { chunk ->
            log.info { chunk }
            listOf<MexInstanceData>().asFlow()
        }

        log.info { "fetchData: $results" }
        return results.asFlow().flattenConcat()
    }

    suspend fun findAll(): List<MexInstance> {
//        val instances = mutableListOf<MexInstance>()
//        for (i in 1..20) {
//            instances.add(MexInstance(i.toString(), "name$i"))
//        }
        return emptyList()
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
}
