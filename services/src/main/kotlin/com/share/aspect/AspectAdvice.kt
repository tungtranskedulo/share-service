package com.share.aspect

import com.share.domain.refresh.services.AsyncFailureService
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.apache.logging.log4j.util.Strings
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.*
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.startCoroutineUninterceptedOrReturn
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn


private val log = KotlinLogging.logger {}

@Suppress("UNCHECKED_CAST")
val ProceedingJoinPoint.coroutineContinuation: Continuation<Any?>
    get() = this.args.last() as Continuation<Any?>

val ProceedingJoinPoint.coroutineArgs: Array<Any?>
    get() = this.args.sliceArray(0 until this.args.size - 1)

suspend fun ProceedingJoinPoint.proceedCoroutine(
    args: Array<Any?> = this.coroutineArgs
): Any? =
    suspendCoroutineUninterceptedOrReturn { continuation ->
        this.proceed(args + continuation)
    }

fun ProceedingJoinPoint.runCoroutine(
    block: suspend () -> Any?
): Any? = block.startCoroutineUninterceptedOrReturn(this.coroutineContinuation)

@Component
@Aspect
class LoggingAspect(
    private val asyncFailureService: AsyncFailureService,
    @Value("\${skedulo.app.config.fetch-timeout-millis}")
    private val fetchTimeoutMillis: Long,
) {

    /**
     * This method uses Around advice which ensures that an advice can run before
     * and after the method execution, to and log the execution time of the method
     * This advice will be applied to all the method which are annotate with the
     * annotation @LogExecutionTime @see com.example.springaop.logging.LogExecutionTime
     *
     * Any mehtod where execution times need to be measure and log, annotate the method with @LogExecutionTime
     * example
     * @LogExecutionTime
     * public void m1();
     *
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.share.aspect.LogExecutionTime)")
    @Throws(Throwable::class)
    fun methodTimeLogger(proceedingJoinPoint: ProceedingJoinPoint): Any? {
        val methodSignature = proceedingJoinPoint.signature as MethodSignature

        // Get intercepted method details
        val className: String = methodSignature.declaringType.simpleName
        val methodName: String = methodSignature.name

        // Measure method execution time
        val stopWatch = StopWatch("$className -> $methodName")
        stopWatch.start(methodName)

        kotlin.runCatching {
            val result: Any? = proceedingJoinPoint.proceed()
            stopWatch.stop()
            if (log.isInfoEnabled) {
                log.info(stopWatch.printExecutionTime(""))
            }
            return result
        }.onFailure {
            stopWatch.stop()
            if (log.isErrorEnabled) {
                log.error(stopWatch.printExecutionTime(""))
                log.error { it }
            }
            throw it
        }
        return null
    }

    /**
     * This method uses Around advice which ensures that an advice can run before
     * and after the method execution, to and log the execution time of the method
     * This advice will be applied to all the method which are annotate with the
     * annotation @LogExecutionTime @see com.example.springaop.logging.LogExecutionTime
     *
     * Any mehtod where execution times need to be measure and log, annotate the method with @LogExecutionTime
     * example
     * @LogExecutionTime
     * public void m1();
     *
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    @Around(
        """
        @annotation(coroutineLogExecutionTime) &&
        args(.., kotlin.coroutines.Continuation)
        """
    )
    @Throws(Throwable::class)
    fun coroutineLogExecutionTime(
        proceedingJoinPoint: ProceedingJoinPoint,
        coroutineLogExecutionTime: CoroutineLogExecutionTime
    ): Any? =
        proceedingJoinPoint.runCoroutine {
            val methodSignature = proceedingJoinPoint.signature as MethodSignature

            // Get intercepted method details
            val className: String = methodSignature.declaringType.simpleName
            val methodName: String = methodSignature.name


            // Measure method execution time
            val stopWatch = StopWatch("$className -> $methodName")
            stopWatch.start(methodName)
            kotlin.runCatching {
                val result: Any? = proceedingJoinPoint.proceedCoroutine()
                stopWatch.stop()
                stopWatch.printSuccessExecutionTime()
                return@runCoroutine result
            }.onFailure { ex ->
                stopWatch.stop()
                if (ex is TimeoutCancellationException) throw ex
                else ex.also { stopWatch.printFailureExecutionTime(it) }.let { throw it }
            }
        }

    @Around(
        """
        @annotation(timeout) &&
        args(.., kotlin.coroutines.Continuation)
        """
    )
    @Throws(Throwable::class)
    fun suspendCoroutineWithTimeout(
        proceedingJoinPoint: ProceedingJoinPoint,
        timeout: WithTimeout
    ): Any? =
        proceedingJoinPoint.runCoroutine {
            val methodSignature = proceedingJoinPoint.signature as MethodSignature

            // Get intercepted method details
            val className: String = methodSignature.declaringType.simpleName
            val methodName: String = methodSignature.name

            val timeoutMillis = getTimeoutMillis(timeout)

            // Measure method execution time
            val stopWatch = StopWatch("$className -> $methodName")
            stopWatch.start(methodName)
            kotlin.runCatching {
                val result = withTimeout(timeoutMillis) {
                    proceedingJoinPoint.proceedCoroutine()
                }
                stopWatch.stop()
                return@runCoroutine result
            }.onFailure { ex ->
                stopWatch.stop()
                ex.also { stopWatch.printFailureExecutionTime(it) }.let { throw it }
            }
        }

    private fun getTimeoutMillis(timeout: WithTimeout): Long {
        return timeout.timeout.takeIf { it > 0 }?.let {
            when (timeout.unit) {
                TimeUnit.SECONDS -> it * 1000L
                TimeUnit.MILLISECONDS -> it
                TimeUnit.HOURS -> it * 1000L * 60L * 60L
                else -> 0L
            }
        } ?: fetchTimeoutMillis
    }

}

class CoroutineTimeout {
    suspend inline fun <T> suspendCoroutineWithTimeout(
        crossinline block: (Continuation<T>) -> Unit
    ) {
        val annotation = this::class.java.getAnnotation(WithTimeout::class.java)
        if (annotation != null) {
            val timeout = annotation.timeout
            withTimeout(timeout) {
                suspendCancellableCoroutine(block = block)
            }
        } else {
            suspendCancellableCoroutine(block = block)
        }
    }
}


private fun StopWatch.printSuccessExecutionTime(message: String = Strings.EMPTY) {
    if (log.isInfoEnabled) {
        log.info(this.printExecutionTime(message))
    }
}

private fun StopWatch.printFailureExecutionTime(error: Throwable) {
    if (log.isErrorEnabled) {
        val sb = StringBuilder()
        sb.append(this.printExecutionTime("Failure"))
        sb.append("; [").append(error.localizedMessage).append("] ")
        log.error { sb.toString() }
    }
}

private fun StopWatch.printExecutionTime(status: String): String {
    val sb = StringBuilder()
    sb.append("StopWatch - ").append("$status '").append(this.id).append("': running time (millis) = ")
        .append(this.totalTimeMillis)
    for (taskInfo in this.taskInfo) {
        sb.append("; [").append(taskInfo.taskName).append("] took ")
            .append(taskInfo.timeMillis).append(" millis = ").append(taskInfo.timeSeconds)
            .append(" sec")
    }
    return sb.toString()
}

