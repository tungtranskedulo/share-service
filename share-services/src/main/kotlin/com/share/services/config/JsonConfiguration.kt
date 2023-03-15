package com.share.services.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class JsonConfiguration {

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper = Json.mapper

}
