package com.share.http

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.share.aspect.BaseApiException
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

private val log = KotlinLogging.logger {}

class WebClientResponseErrorFilter : ExchangeFilterFunction {
    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        log.info { "Request: $request" }
        return next.exchange(request).flatMap { clientResponse ->
            log.info { "clientResponse: $clientResponse" }
            if (clientResponse.statusCode().is4xxClientError) {
                clientResponse.bodyToMono<String>()
                    .defaultIfEmpty("")
                    .flatMap { _ ->
                        clientResponse.toSkeduloApiClientException()
                    }
            } else {
                Mono.just(clientResponse)
            }
        }
    }

    private fun ErrorDetails.toSkeduloApiClientException(status: HttpStatus): Mono<ClientResponse> = let { source ->
        return Mono.error(
            SkeduloApiClientException(
                httpStatus = status,
                errorDetails = source
            )
        )
    }

    private fun ClientResponse.toSkeduloApiClientException(): Mono<ClientResponse> {
        return createException().flatMap { exception ->
            Mono.error(
                SkeduloApiClientException(
                    httpStatus = exception.statusCode,
                    message = exception.message ?: ""
                )
            )
        }
    }
}

class SkeduloApiClientException(
    httpStatus: HttpStatus,
    errorType: String,
    message: String,
    val extraFields: Map<String, Any> = hashMapOf()
) : BaseApiException(
    httpStatus = httpStatus,
    errorType = errorType,
    message = message
) {

    constructor(httpStatus: HttpStatus, errorDetails: ErrorDetails) : this(
        httpStatus = httpStatus,
        errorType = errorDetails.errorType,
        message = errorDetails.message,
        extraFields = errorDetails.extraFields
    )

    constructor(httpStatus: HttpStatus, message: String) : this(
        httpStatus = httpStatus,
        errorType = httpStatus.name.lowercase(),
        message = message
    )
}
data class ErrorDetails(
    val errorType: String,
    val message: String,
    // This has to have default of hashMap or decoding fails
    @JsonAnySetter
    @get:JsonAnyGetter
    val extraFields: Map<String, Any> = hashMapOf()
)
