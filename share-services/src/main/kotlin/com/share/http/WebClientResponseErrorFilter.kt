package com.share.http

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.share.aspect.BaseApiException
import org.springframework.http.HttpStatus

//class WebClientResponseErrorFilter : ExchangeFilterFunction {
//    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
//        return next.exchange(request).flatMap { clientResponse ->
//            if (clientResponse.statusCode().is4xxClientError) {
//                clientResponse.bodyToMono<String>()
//                    .defaultIfEmpty("")
//                    .flatMap { body ->
//                        clientResponse.toSkeduloApiClientException()
//                    }
//            } else {
//                Mono.just(clientResponse)
//            }
//        }
//    }
//
//    private fun ErrorDetails.toSkeduloApiClientException(status: HttpStatus): Mono<ClientResponse> = let { source ->
//        return Mono.error(
//            SkeduloApiClientException(
//                httpStatus = status,
//                errorDetails = source
//            )
//        )
//    }
//
//    private fun ClientResponse.toSkeduloApiClientException(): Mono<ClientResponse> {
//        return createException().flatMap { exception ->
//            Mono.error(
//                SkeduloApiClientException(
//                    httpStatus = exception.statusCode,
//                    message = exception.message ?: ""
//                )
//            )
//        }
//    }
//}

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
