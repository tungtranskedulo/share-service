package com.share.logging

import mu.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
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
class LoggingAspect {
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
    @Around("@annotation(com.share.logging.LogExecutionTime)")
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
                log.info(stopWatch.printExecutionTime())
            }
            return result
        }.onFailure {
            stopWatch.stop()
            if (log.isErrorEnabled) {
                log.error(stopWatch.printExecutionTime())
                log.error { it }
            }
            throw it
        }
        return null
    }
}

@Component
@Aspect
class CoroutineLoggingAspect {
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
        @annotation(com.share.logging.CoroutineLogExecutionTime) &&
        args(.., kotlin.coroutines.Continuation)
        """
    )
    @Throws(Throwable::class)
    fun methodTimeLogger(proceedingJoinPoint: ProceedingJoinPoint): Any? =
        proceedingJoinPoint.runCoroutine {
            val methodSignature = proceedingJoinPoint.signature as MethodSignature

            // Get intercepted method details
            val className: String = methodSignature.declaringType.simpleName
            val methodName: String = methodSignature.name

            // Measure method execution time
            val stopWatch = StopWatch("$className -> $methodName")
            stopWatch.start(methodName)

            val result: Any? = proceedingJoinPoint.proceedCoroutine()
            stopWatch.stop()
            if (log.isInfoEnabled) {
                log.info(stopWatch.printExecutionTime())
            }
            result
        }
}


fun StopWatch.printExecutionTime(): String {
    val sb = StringBuilder()
    sb.append("StopWatch '").append(this.id).append("': running time (millis) = ")
        .append(this.totalTimeMillis)
    for (taskInfo in this.taskInfo) {
        sb.append("; [").append(taskInfo.taskName).append("] took ")
            .append(taskInfo.timeMillis).append(" millis = ").append(taskInfo.timeSeconds)
            .append(" sec")
    }
    return sb.toString()
}

