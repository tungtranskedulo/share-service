package com.share

import com.share.http.api.ApiSuccessResult
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.netty.http.client.HttpClientRequest
import java.time.Duration


@Component
class ApiClient(
    @Qualifier("mocApiClient")
    private val mocApiClient: WebClient
) {
    suspend fun getMexEngine(
        appVersion: String,
        platform: String,
    ): String {
        val request = mocApiClient.get()
            .uri("/form/bundle/engine/$appVersion/$platform")
//            .httpRequest { httpRequest ->
//                val reactorRequest = httpRequest.getNativeRequest() as HttpClientRequest
//                reactorRequest.responseTimeout(Duration.ofSeconds(2))
//            }

        return request
            .retrieve()
            .awaitBody<ApiSuccessResult<String>>()
            .result
    }

}
