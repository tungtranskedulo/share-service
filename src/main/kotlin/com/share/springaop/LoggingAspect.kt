package com.share.springaop

import mu.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch

private val log = KotlinLogging.logger {}

@Component
@Aspect
class LoggingAspect {
    /**
     * This method uses Around advice which ensures that an advice can run before
     * and after the method execution, to and log the execution time of the method
     * This advice will be be applied to all the method which are annotate with the
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
    @Around("@annotation( com.share.springaop.LogExecutionTime)")
    @Throws(Throwable::class)
    fun methodTimeLogger(proceedingJoinPoint: ProceedingJoinPoint) : Any? {
        kotlin.runCatching {
            val methodSignature: MethodSignature = proceedingJoinPoint.signature as MethodSignature

            // Get intercepted method details
            val className: String = methodSignature.declaringType.simpleName
            val methodName: String = methodSignature.name

            // Measure method execution time
            val stopWatch = StopWatch("$className->$methodName")
            stopWatch.start(methodName)
            val result: Any? = proceedingJoinPoint.proceed()
            stopWatch.stop()
            // Log method execution time
            if (log.isInfoEnabled) {
                log.info(stopWatch.printExecutionTime())
            }
            return result
        }.getOrElse {
            return null
        }
    }

    private fun StopWatch.printExecutionTime(): String {
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
}