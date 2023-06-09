package com.share.domain.refresh.services

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.share.aspect.AsyncException
import com.share.aspect.CoroutineLogExecutionTime
import com.share.cache.cachedBy
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
    private val fetchParallel: FetchParallel
) {
    private val asyncExceptionCache: Cache<String, Any> = Caffeine
        .newBuilder()
        .expireAfterWrite(15, TimeUnit.SECONDS)
        .build()

    //@CoroutineLogExecutionTime
    suspend fun refreshData() {
        val userMetadata = getUserMetadata()

        val refreshScope = CoroutineScope(
            Dispatchers.IO
                    + MDCContext()
                    // catch any exceptions that bubble up
                    + refreshUserDataExceptionHandler(userMetadata)
                    // allow jobs to fail independently, one failure will not fail all
                    + SupervisorJob()
        )

        refreshScope.launch {
                waitForJobsToFinish(
                   // launch() { cacheService.get() },
                   // launch() { menuService.fetchMenu() },
                    launch() { fetchParallel.fetchAllInstanceDataForJob() }
                )
//                waitForJobsToFinish(
//                    launch() { customFormService.getCustomForm2() }
//                )

                log.info { "Done refreshing scope" }
        }.also {
            log.info { "Launched refresh scope" }
        }
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
//            val result = withTimeout(3000) {
//                return@withTimeout jobService.fetchJob()
//            }
//            return result
        } catch (e: Exception) {
            log.error { "Failed to fetch job" }
            return null
        }
        return null
        //return jobService.fetchJob()
    }

    private suspend fun getUserMetadata(): UserMetadata {
        return UserMetadata(UUID.randomUUID().toString())
    }

    private suspend fun waitForJobsToFinish(vararg jobs: Job) {
        jobs.forEach { job -> job.join() }
    }

}
