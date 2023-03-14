package com.share.services

import com.share.springaop.LogExecutionTime
import kotlinx.coroutines.*
import kotlinx.coroutines.slf4j.MDCContext
import org.springframework.stereotype.Service

@Service
class DataRefreshService(
    private val customFormService: CustomFormService
) {

    @LogExecutionTime
    fun refreshData()   {
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
        }
    }

    private suspend fun waitForJobsToFinish(vararg jobs: Job) {
        jobs.forEach { job -> job.join() }
    }

}