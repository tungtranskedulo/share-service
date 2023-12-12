package com.share.domain.refresh.services

import com.share.Util.waiForRefreshScope
import com.share.Util.waitForJobsToFinish
import com.share.aspect.CoroutineLogExecutionTime
import com.share.aspect.WithTimeout
import com.share.model.UserMetadata
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}

@Service
class TimeLineServices {

    @CoroutineLogExecutionTime
    @WithTimeout
    suspend fun fetchTimeLineData(refreshScope: CoroutineScope) {
        processData(refreshScope)
    }

    private suspend fun processData() {
        waiForRefreshScope({ refreshTimeLineServiceExceptionHandler() }) { refreshScope ->
            waitForJobsToFinish(
                refreshScope.launch { persist() },
                refreshScope.launch { fetchTravelDistance() },
            )
        }
    }

    private suspend fun processData(refreshScope: CoroutineScope) {
        extractFromResourcesData()

        waitForJobsToFinish(
            refreshScope.launch { persist() },
            refreshScope.launch { fetchTravelDistance() },
        )
    }

    private suspend fun extractFromResourcesData() {
        delay(1000)
        log.info("extractFromResourcesData")
        // RuntimeException("extractFromResourcesData")
    }

    private fun refreshTimeLineServiceExceptionHandler() =
        CoroutineExceptionHandler { _, throwable ->
            log.error(throwable) {
                "Caught exception at timeline service "
            }
        }

    private suspend fun persist() {
        log.info("persist data")
        saveJobs()
        saveActivities()
    }
    private suspend fun persist(refreshScope: CoroutineScope) {
        log.info("persist data")
        waitForJobsToFinish(
            refreshScope.launch { saveJobs() },
            refreshScope.launch { saveActivities() },
        )
    }

    private suspend fun fetchTravelDistance() {
        delay(1000)
        log.info("fetchTravelDistance")
    }

    private suspend fun saveJobs() {
        delay(1000)
        log.info("saveJobs")
        throw RuntimeException("saveJobs")
    }

    private suspend fun saveActivities() {
        delay(1000)
        log.info("saveActivities")
    }
}
