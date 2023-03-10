package com.share.service.common

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoUnit
import kotlin.time.Duration

fun parseIsoTime(s: String): ZonedDateTime? =
    s.runCatching {
        ZonedDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)
    }.getOrNull()

fun parseIsoInstant(s: String): Instant? =
    parseIsoTime(s)?.toInstant()

fun Instant.toIsoString(): String =
    DateTimeFormatterBuilder().appendInstant(3).toFormatter().format(this)

fun LocalDate.toIsoString(): String =
    format(DateTimeFormatter.ISO_DATE)

fun Instant.plusMinutes(minutesToAdd: Int) =
    plus(minutesToAdd.toLong(), ChronoUnit.MINUTES)

fun Instant.minusMinutes(minutesToSubtract: Int) =
    minus(minutesToSubtract.toLong(), ChronoUnit.MINUTES)

fun Instant.plusHours(hoursToAdd: Int) =
    plus(hoursToAdd.toLong(), ChronoUnit.HOURS)

fun Instant.minusHours(hoursToSubtract: Int) =
    minus(hoursToSubtract.toLong(), ChronoUnit.HOURS)

fun Instant.atStartOfDay(): Instant =
    truncatedTo(ChronoUnit.DAYS)

operator fun Instant.plus(duration: Duration): Instant =
    plus(duration.inWholeNanoseconds, ChronoUnit.NANOS)

operator fun Instant.minus(duration: Duration): Instant =
    minus(duration.inWholeNanoseconds, ChronoUnit.NANOS)