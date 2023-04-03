package com.share.domain.refresh.services

import com.share.logging.CoroutineLogExecutionTime
import com.share.logging.LogExecutionTime
import kotlinx.coroutines.*
import kotlinx.coroutines.slf4j.MDCContext
import mu.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

private val log = KotlinLogging.logger {}

@Service
class DataRefreshService(
    private val customFormService: CustomFormService
) {

    //@CoroutineLogExecutionTime
    suspend fun refreshData()   {
        val refreshScope = CoroutineScope(
            Dispatchers.Default
                    + MDCContext()
                    + SupervisorJob()
        )

        refreshScope.launch {
            waitForJobsToFinish(
                launch { customFormService.getCustomForm() },
                launch { customFormService.getCustomForm2() }
            )
            delay(1000)
        }
    }

    private suspend fun waitForJobsToFinish(vararg jobs: Job) {
        jobs.forEach { job -> job.join() }
    }

}
