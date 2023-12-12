package com.share.domain.refresh.services

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.share.aspect.AsyncException
import com.share.aspect.CoroutineLogExecutionTime
import com.share.cache.cachedBy
import com.share.config.JsonObject
import com.share.config.emptyJsonObject
import com.share.model.ModelWithGlobalSequence
import com.share.model.UserMetadata
import kotlinx.coroutines.*
import kotlinx.coroutines.slf4j.MDCContext
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.rmi.server.UID
import java.util.*
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}

@Service
class DataRefreshService(
    private val customFormService: CustomFormService,
    private val jobService: JobService,
    private val menuService: MenuService,
    private val asyncFailureService: AsyncFailureService,
    private val cacheService: CacheService,
    private val fetchParallel: FetchParallel,
    private val mexHostService: MexHostService,
    private val timeLineServices: TimeLineServices
) {
    private val asyncExceptionCache: Cache<String, Any> = Caffeine
        .newBuilder()
        .expireAfterWrite(15, TimeUnit.SECONDS)
        .build()

    suspend fun refreshData() {
        val userMetadata = getUserMetadata()

        withRefreshScope({ refreshUserDataExceptionHandler(userMetadata) }) { refreshScope ->
            waitForJobsToFinish(
                refreshScope.launch() { customFormService.saveCustomFormData("formId1", emptyJsonObject()) },
            )

            log.info { "Done refreshData refresh scope" }
        }.also {
            log.info { "Launched refreshData scope" }
        }
    }

    suspend fun run() : Any {
        //return customFormService.saveCustomFormData("formId1", emptyJsonObject())
        return customFormService.processEventsForDevice()
    }

    private suspend fun waitForJobsToFinish(vararg jobs: Job) {
        jobs.forEach { job -> job.join() }
    }


    private fun refreshUserDataExceptionHandler(userMetadata: UserMetadata) =
        CoroutineExceptionHandler { _, throwable ->
            //handleAsyncException(throwable)
            log.error(throwable) {
                "Caught exception for [tenantId=${userMetadata.id}]"
            }
        }

    private fun handleAsyncException(ex: Throwable) {
        runBlocking { asyncFailureService.saveAsync(listOf(ex)) }
    }

    fun cacheAsyncException(key: String, exception: Throwable) {
        val ex = asyncExceptionCache.cachedBy(
            key = key,
            mergeValue = exception
        ) {
            listOf(exception)
        }
        log.info { "cached $ex" }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun handleAsyncExceptionIfPresent(key: String) {
        val exception = asyncExceptionCache.getIfPresent(key)
        if (exception != null && exception is List<*>) {
            log.error { "Async exception found for key $key" }
            asyncFailureService.saveAsync(exception as List<Throwable>)
            asyncExceptionCache.invalidate(key)
        }
        asyncExceptionCache.getIfPresent(key).let {
            log.info { "Async exception delete check $it" }
        }
    }

    @CoroutineLogExecutionTime
    suspend fun refreshEntity(): ModelWithGlobalSequence<*>? {
        try {
            // todo - add logic to fetch entity
        } catch (e: Exception) {
            log.error { "Failed to fetch job" }
            return null
        }
        return null
    }

    private suspend fun getUserMetadata(): UserMetadata {
        return UserMetadata(UUID.randomUUID().toString())
    }

    private suspend fun <T> withRefreshScope(
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
        refreshScope.launch {
            block(refreshScope)
        }
    }

}
