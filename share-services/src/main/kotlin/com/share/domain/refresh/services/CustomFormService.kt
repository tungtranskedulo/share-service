package com.share.domain.refresh.services

import com.share.logging.CoroutineLogExecutionTime
import com.share.logging.LogExecutionTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

private val log = KotlinLogging.logger {}
@Service
class CustomFormService {
    @CoroutineLogExecutionTime
     suspend fun getCustomForm()  {
        execute()
    }

    suspend fun execute() {
        delay(2000)
    }


    @LogExecutionTime
    fun getCustomForm2()  {
        kotlin.runCatching {
            Thread.sleep(1000)
        }.getOrElse {
            throw it
        }
    }
}
