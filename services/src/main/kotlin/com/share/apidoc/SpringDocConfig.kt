package com.share.apidoc

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SpringDocConfig {
    @Bean
    fun publicApi(): GroupedOpenApi {
        return GroupedOpenApi.builder().group("share-services")
            .pathsToMatch("/share-services/**")
            .build()
    }

    @Bean
    fun openApi(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Share Services")
                    .description("Share Services API endpoints<br/><h3>Service: share services</h3><a href=\"https://docs.google.com/spreadsheets/d/16HaAB8CAg4_Rn-K0wpslBloNqj1_XFTHscXgVDvnkmo/edit#gid=86478019\">Share Services API & Message Broker Contract</a>")
                    .version("v1.0")
                    .license(License().name("Apache License 2.0").url("https://www.apache.org/licenses/LICENSE-2.0.html"))
            ).servers(listOf(
                Server().url("https://fspkyc.fsp.stg-tvlk.cloud/").description("Private Staging Host"),
                Server().url("https://api-public.fsp.staging-traveloka.com/").description("Public Staging Host"),
                Server().url("https://fspkyc.fsp.tvlk.cloud/").description("Private Production Host"),
                Server().url("https://api-public.fsp.traveloka.com/").description("Public Production Host"),
            ))
    }
}

