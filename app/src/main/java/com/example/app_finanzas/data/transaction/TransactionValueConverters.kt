package com.example.app_finanzas.data.transaction

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val monthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

fun Double.toCents(): Long {
    val normalized = BigDecimal(this.toString())
    return normalized
        .movePointRight(2)
        .setScale(0, RoundingMode.HALF_UP)
        .abs()
        .longValueExact()
}

fun Long.toAmount(): Double {
    return BigDecimal(this)
        .movePointLeft(2)
        .toDouble()
}

fun calculateMonthKey(date: String): String {
    return runCatching { LocalDate.parse(date).format(monthFormatter) }
        .getOrElse {
            if (date.length >= 7) {
                date.substring(0, 7)
            } else {
                "1970-01"
            }
        }
}
