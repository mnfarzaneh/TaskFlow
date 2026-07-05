package com.mnfarzaneh.taskflow.utils

import java.time.Instant
import java.time.ZoneId

fun Int.toPersian(): String = this.toString().toPersianDigits()
fun Long.toPersian(): String = this.toString().toPersianDigits()

fun String.toPersianDigits(): String = this
    .replace('0', '۰').replace('1', '۱').replace('2', '۲')
    .replace('3', '۳').replace('4', '۴').replace('5', '۵')
    .replace('6', '۶').replace('7', '۷').replace('8', '۸')
    .replace('9', '۹')

fun formatPersianDate(timestamp: Long): String {
    val localDate = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    val persian = localDate.toPersianCalendar()

    val localDateTime = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()

    val hour   = localDateTime.hour.toPersian().padStart(2, '۰')
    val minute = localDateTime.minute.toString().padStart(2, '0').toPersianDigits()

    return "${persian.year.toPersian()}/${persian.month.toString().padStart(2, '0').toPersianDigits()}/${persian.dayOfMonth.toString().padStart(2, '0').toPersianDigits()} $hour:$minute"
}

fun formatPersianDateShort(timestamp: Long): String {
    val localDate = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    val persian = localDate.toPersianCalendar()
    return "${persian.dayOfMonth.toPersian()} ${persianMonthName(persian.month)}"
}