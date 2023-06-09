package com.share.domain.refresh.services

import arrow.core.Either
import arrow.core.right
import com.fasterxml.jackson.databind.JsonNode
import com.share.config.objectMapper
import com.share.config.readValue
import com.share.config.toJsonObj
import com.share.http.api.ApiSuccessResult
import com.share.aspect.CoroutineLogExecutionTime
import com.share.aspect.WithTimeout
import com.share.model.*
import io.github.bucket4j.util.concurrent.BatchHelper.async
import kotlinx.coroutines.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}

@Service
class CustomFormService(
    @Value("\${skedulo.app.config.limit-concurrent-coroutine}")
    private val limitConcurrentCoroutine: Int,
) {
    @CoroutineLogExecutionTime
    suspend fun getCustomForm() {
        log.info { "Fetching custom form" }
        delay(500)
    }

    @CoroutineLogExecutionTime
    //@WithTimeout(timeout = 1, unit = TimeUnit.SECONDS)
    suspend fun getCustomForm2(a: String, b: Int) : Map<String, Any> {
        log.info { "Fetching custom form2" }
        return mapOf("a" to "b")
    }

    suspend fun processEventsForDevice() {
        log.info { "Processing events for device " }
        var process = true
        while (process) {
            when (processOneEvent()) {
                ShouldContinue.STOP -> {
                    log.info { "Done processing events" }
                    process = false
                }

                else -> {}
            }
        }
        log.info { "exiting process Events" }
    }

    suspend fun processOneEvent(): ShouldContinue {
        return try {
            //val sequence = 1
            val nextEvent = getNextEvent().orNull()

            if (nextEvent == null) {
                log.info { "No more events to process." }
                ShouldContinue.STOP
            } else {
                //ShouldContinue.CONTINUE
                ShouldContinue.STOP
            }
        } catch (t: Throwable) {
            log.warn(t) { "Error in processing events" }
            ShouldContinue.STOP
        }
    }


    suspend fun getNextEvent(): Either<Error, ClientEvent?> {
        val nextEventList = getNextEventList()

        return if (nextEventList.isEmpty()) {
            null.right()
        } else {
            val result = getClientEventFromQueryResult(nextEventList[0])
            log.info { "Found event $result" }
            result.right()
        }
    }

    suspend fun getNextEventList(): List<NextEventQueryResult> {

        val nextEventList = listOf(
            NextEventQueryResult(
                "1",
                objectMapper.readValue("""{"clientSequence":24,"data":{"isDraft":false,"objectId":"00141632-52f0-4e9c-a269-36d829155ca6","operation":"Updated","postChangeData":{"customFields":{"custom_boolean":false}},"preChangeData":{"customFields":{"custom_boolean":true}},"schema":"Resources","type":"Write","typeHint":"Resources","writeEventAction":"CustomField"},"deviceIdentifier":"450C72BE-B6FD-46A5-91B6-F620807FAEF4","eventSchemaVersion":1,"isDraft":false,"projectionId":"sk_f68824f0fd34429eab6861d610578779|00141632-52f0-4e9c-a269-36d829155ca6","resourceId":"00053e7f-2057-4959-88a6-8c1378d3c85f","sessionIdentifier":"EA953FC7-B398-41ED-A70F-66C36E490667","tenantId":"sk_f68824f0fd34429eab6861d610578779","timeWritten":"2023-05-05T08:11:33.797Z","type":"ClientEvent","uid":"36633b80-b30b-4293-bee5-2d18b819f880"}""")
            )
        )

        return nextEventList.ifEmpty {
            emptyList()
        }
    }

    suspend fun getClientEventFromQueryResult(
        result: NextEventQueryResult
    ): ClientEvent {
        val evJson = result.eventJson
        val event = if (evJson.get("eventId") == null) {
            evJson.put("eventId", result.id)
        } else {
            evJson
        }
        return try {
            objectMapper.convertValue(event, ClientEvent::class.java)
        } catch (error: Throwable) {
            val fields: Iterator<MutableMap.MutableEntry<String, JsonNode>> = event.fields()
            while (fields.hasNext()) {
                val field = fields.next()
                if (field.key == "data") {
                    val data = ClientEvent.Data.MalFormedData(
                        detailedMessage = error.localizedMessage,
                        causeFields = error.cause?.cause?.stackTrace?.get(0)?.className?.split(".")?.last() ?: "Unknown"
                    )
                    field.setValue(data.toJsonObj())
                }
            }
            objectMapper.convertValue(event, ClientEvent::class.java)
        }
    }

    @CoroutineLogExecutionTime
    suspend fun fetchAllInstanceDataForJob(): List<MexInstanceData> {
        val fetchData = findAll().pmap {
            getMexFetchData(it.id)
        }

        return fetchData.filterNotNull().toList()
    }

    suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
        val requestSemaphore = Semaphore(FetchParallel.limitConcurrentCoroutine)
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
        return try {
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

