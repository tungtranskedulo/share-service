package com.share.services

import com.share.springaop.LogExecutionTime
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}
@Service
class CustomFormService {
    @LogExecutionTime
    fun getCustomForm()  {
        Thread.sleep(1000)
    }

    @LogExecutionTime
    fun getCustomForm2()  {
        kotlin.runCatching {
            Thread.sleep(1000)
            val a = 10/0
            log.info { a }
        }.getOrElse {
            throw it
        }
    }
}