package com.share.domain.refresh.services

import com.share.ApiClient
import com.share.aspect.*

import com.share.model.Job
import com.share.model.ResourceId
import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

private val log = KotlinLogging.logger {}

@Service
class JobService(
    private val apiClient: ApiClient
) {

    //@CoroutineLogExecutionTime
    @WithTimeout(2, unit = TimeUnit.SECONDS)
    suspend fun fetchJob() {
        log.info { "Fetching job" }
        //delay(1000)
        //throw Exception("Failed to fetch job")
        //delay(1000)
        jobDetails()
    }

    suspend fun jobDetails() {
        log.info { "Fetching job details" }
        delay(1500)
        //throw Exception("Failed to fetch job details")
    }

}
