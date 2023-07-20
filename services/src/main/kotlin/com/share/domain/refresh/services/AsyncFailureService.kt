package com.share.domain.refresh.services

import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Service
class AsyncFailureService {
    suspend fun saveAsync(error: List<Throwable>) {
        delay(1000)
        error.map {
            log.error { "saved async error ${it.message}" }
        }
    }
}
