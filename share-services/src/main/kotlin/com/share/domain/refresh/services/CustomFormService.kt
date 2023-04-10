package com.share.domain.refresh.services

import com.share.logging.CoroutineLogExecutionTime
import kotlinx.coroutines.delay

import mu.KotlinLogging
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Service
class CustomFormService {
    @CoroutineLogExecutionTime
    suspend fun getCustomForm(): String {
        return execute()
    }

    suspend fun execute(): String {
        delay(1000)
       // throw RuntimeException("c")
        return execute1()
    }

    suspend fun execute1(): String {
        delay(1000)
        log.info { "execute1" }
        return "execute1"
    }

    //@CoroutineLogExecutionTime
    suspend fun getCustomForm1() {
        delay(400)
        //execute()
    }


    // @LogExecutionTime
    fun getCustomForm2() {
        kotlin.runCatching {
            Thread.sleep(1000)
        }.getOrElse {
            throw it
        }
    }
}
