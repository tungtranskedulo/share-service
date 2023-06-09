package com.share.config


import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.Connection
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.TcpClient
import java.time.Duration
import java.util.concurrent.TimeUnit


@Configuration
class ServerApiClientConfig(
) {

    @Bean
    fun mocApiClient(): WebClient = createWebClient()

    private fun createWebClient(): WebClient {
        val timeoutSecond = 2
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
            .build()
    }
}

