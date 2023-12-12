package com.share

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext

object Util {
    suspend fun waitForJobsToFinish(vararg jobs: Job) {
        jobs.forEach { job -> job.join() }
    }

    suspend fun <T> waiForRefreshScope(
        dataExceptionHandler: () -> CoroutineExceptionHandler,
        block: suspend (CoroutineScope) -> T
    ) {
        val refreshScope = CoroutineScope(
            Dispatchers.IO
                    + MDCContext()
                    // catch any exceptions that bubble up
                    + dataExceptionHandler()
                    // allow jobs to fail independently, one failure will not fail all
                    + SupervisorJob()
        )
        // return as quickly as possible without waiting for processes launched within to finish
        block(refreshScope)
    }
}
