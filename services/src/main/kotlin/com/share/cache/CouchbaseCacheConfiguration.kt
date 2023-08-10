package com.share.cache


import org.springframework.cache.interceptor.SimpleKey
import org.springframework.core.convert.ConversionService
import org.springframework.core.convert.converter.ConverterRegistry
import org.springframework.format.support.DefaultFormattingConversionService
import java.nio.charset.StandardCharsets
import java.time.Duration

class CouchbaseCacheConfiguration private constructor(
    val expiry: Duration,
    val allowCacheNullValues: Boolean,
    val conversionService: ConversionService,
   // val valueTranscoder: Transcoder
) {


    fun collection(): CouchbaseCacheConfiguration {
        return CouchbaseCacheConfiguration(
            expiry, allowCacheNullValues, conversionService
        )
    }

    companion object {
        fun defaultCacheConfig(expiry: Duration): CouchbaseCacheConfiguration {
            val conversionService = DefaultFormattingConversionService()
            registerDefaultConverters(conversionService)
            return CouchbaseCacheConfiguration(
                expiry,
                true,
                conversionService,
               // SerializableTranscoder.INSTANCE
            )
        }

        private fun registerDefaultConverters(registry: ConverterRegistry) {
            registry.addConverter(
                String::class.java,
                ByteArray::class.java
            ) { source: String ->
                source.toByteArray(
                    StandardCharsets.UTF_8
                )
            }
            registry.addConverter(
                SimpleKey::class.java,
                String::class.java
            ) { obj: SimpleKey -> obj.toString() }
        }
    }
}
