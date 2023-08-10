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
    private val apiClient: ApiClient,
    private val customFormService: CustomFormService,
) {

    @CoroutineLogExecutionTime
    //@WithTimeout(timeout = 1, unit = TimeUnit.SECONDS)
    suspend fun fetchMenu() : List<String> {
        log.info { "Fetching menu" }
        return getDefinitions()
    }

    suspend fun getDefinitions(): List<String> {
        delay(1000)
        return listOf("a", "b")
    }

    @CoroutineLogExecutionTime
    suspend fun fetchMenu2() {
        log.info { "Fetching menu2" }
        delay(1500)
        log.info { "Done Fetching menu2" }
    }

}
