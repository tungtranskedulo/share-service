package com.share.aspect

import java.util.concurrent.TimeUnit


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class LogExecutionTime

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CoroutineLogExecutionTime(
    val withTimeout: Boolean = false,
    val timeout: Long = 0,
    val unit: TimeUnit = TimeUnit.SECONDS
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class WithTimeout(val timeout: Long = 0, val unit: TimeUnit = TimeUnit.SECONDS)

