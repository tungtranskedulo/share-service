package com.share.domain.refresh.services

import com.share.logging.LogExecutionTime
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
        }.getOrElse {
            throw it
        }
    }
}