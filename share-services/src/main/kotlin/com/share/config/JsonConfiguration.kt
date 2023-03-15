package com.share.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.share.config.Json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class JsonConfiguration {

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper = Json.mapper

}
