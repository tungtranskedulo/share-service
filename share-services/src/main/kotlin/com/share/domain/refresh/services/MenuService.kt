package com.share.domain.refresh.services

import com.share.ApiClient
import com.share.aspect.CoroutineLogExecutionTime
import com.share.aspect.WithTimeout
import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}

@Service
class MenuService(
    private val apiClient: ApiClient
) {

    @CoroutineLogExecutionTime
    @WithTimeout(timeout = 1, unit = TimeUnit.SECONDS)
    suspend fun fetchMenu() {
        log.info {  "Fetching menu" }
        delay(1500)
        log.info {  "Done Fetching menu" }
       // throw Exception("Failed to fetch menu")
    }

}
