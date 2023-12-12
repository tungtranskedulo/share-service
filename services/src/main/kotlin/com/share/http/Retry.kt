package com.share.http

import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import io.github.resilience4j.retry.event.RetryEvent
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.Duration
import java.util.function.Predicate

private val log = KotlinLogging.logger {}

@ConstructorBinding
@ConfigurationProperties("skedulo.app.retry-policy.condenser")
data class CondenserRetryPolicy(
    val maxAttempts: Int = 2,
    val waitDurationSeconds: Long = 2
)

@Configuration
class RetryConfiguration {

    @Autowired
    private lateinit var condenserRetryPolicy: CondenserRetryPolicy


    @Bean
    fun retryPolicyConfiguration(): Map<RetryPolicyName, RetryConfig> {
        return mapOf(
            RetryPolicyName.Condenser to RetryPolicy.condenserRetryPolicy(
                maxAttempts = condenserRetryPolicy.maxAttempts,
                waitDurationSeconds = condenserRetryPolicy.waitDurationSeconds,
            )
        )
    }
}

enum class RetryPolicyName {
    Condenser,
    Default,
}

class RetryPolicy {

    companion object {
        fun defaultRetryPolicy(
            maxAttempts: Int,
            waitDurationSeconds: Long,
            retryExceptions: List<Class<out Throwable>>
        ): RetryConfig {
            return RetryConfig.custom<Any>()
                .maxAttempts(maxAttempts)
                .waitDuration(Duration.ofSeconds(waitDurationSeconds))
                .retryExceptions(*retryExceptions.toTypedArray())
                .build()
        }

        fun condenserRetryPolicy(
            maxAttempts: Int,
            waitDurationSeconds: Long,
        ): RetryConfig {
            return RetryConfig.custom<Any>()
                .maxAttempts(maxAttempts)
                .waitDuration(Duration.ofSeconds(waitDurationSeconds))
                .retryOnException {
                    it is WebClientResponseException && it.statusCode in listOf(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        HttpStatus.GATEWAY_TIMEOUT
                    )
                }
                .build()
        }
    }
}

suspend fun <T> retryWithPolicy(retryPolicy: RetryConfig, block: suspend () -> T): T {
    val retryRegistry = RetryRegistry.of(retryPolicy)
    val retry = retryRegistry.retry("retry")
    retry.eventPublisher
        .onRetry { event ->
            log.info(
                "Request failed with ${event.lastThrowable?.message}. Retry attempt ${event.numberOfRetryAttempts}"
            )
        }

    val decorateFunction = Retry.decorateSupplier(retry) {
        runBlocking {
            block()
        }
    }
    val result = decorateFunction.get()
    log.info { "Result: $result" }
    return result
}
