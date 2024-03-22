package com.share.config


import com.share.http.WebClientResponseErrorFilter
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.Connection
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.TcpClient
import java.time.Duration
import java.util.concurrent.TimeUnit


@Configuration
class ServerApiClientConfig(
    @Qualifier("authIdToken")
    private val authIdToken: AuthIdToken
) {

    @Bean
    fun mocApiClient(): WebClient = createWebClient()

    @Bean
    fun condenserApiClient(@Autowired authIdToken: AuthIdToken): WebClient = mocApiClient().mutate()
        .filter { request, next ->
            val updatedRequest = ClientRequest.from(request)
                .headers{ header ->
                    header.add("X-Auth-IdToken", authIdToken.getIdToken())
                }
                .build()
            next.exchange(updatedRequest)
        }
        .filter { request, next ->
            val updatedRequest = ClientRequest.from(request)
                .headers{ header ->
                    header.add("Test", authIdToken.getIdToken())
                }
                .build()
            next.exchange(updatedRequest)
        }
        .build()

//    @Autowired
//    lateinit var authIdToken: AuthIdToken


    private fun createWebClient(): WebClient {
        val timeoutSecond = 60
        val httpClient: HttpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutSecond * 1000)
            .doOnConnected { conn ->
                conn.addHandlerLast(ReadTimeoutHandler(timeoutSecond))
                    .addHandlerLast(WriteTimeoutHandler(timeoutSecond))
            }
            .responseTimeout(Duration.ofSeconds(timeoutSecond.toLong()))

        return  WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .baseUrl("http://localhost:3000")
            .defaultCookie("cookie-name", "cookie-value")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .filter(WebClientResponseErrorFilter())
            .build()
    }
}

@Component
class AuthIdToken {
    private var idToken: String = ""

    fun getIdToken(): String {
        return idToken
    }

    fun setIdToken(newIdToken: String) {
        idToken = newIdToken
    }
}

