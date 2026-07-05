package com.mnfarzaneh.taskflow.utils

import java.time.LocalDate

data class PersianDate(val year: Int, val month: Int, val dayOfMonth: Int) {

    val monthLength: Int get() = when {
        month <= 6  -> 31
        month <= 11 -> 30
        else        -> if (isPersianLeapYear(year)) 30 else 29
    }

    // شنبه=1 ... جمعه=7
    val dayOfWeek: Int get() {
        val civil = toLocalDate()
        return when (civil.dayOfWeek.value) {
            6    -> 1  // شنبه
            7    -> 2  // یکشنبه
            1    -> 3  // دوشنبه
            2    -> 4  // سه‌شنبه
            3    -> 5  // چهارشنبه
            4    -> 6  // پنجشنبه
            5    -> 7  // جمعه
            else -> 1
        }
    }

    fun toLocalDate(): LocalDate = jalaliToGregorian(year, month, dayOfMonth)
}

fun LocalDate.toPersianCalendar(): PersianDate = gregorianToJalali(year, monthValue, dayOfMonth)

fun persianMonthName(month: Int): String = when (month) {
    1  -> "فروردین"; 2  -> "اردیبهشت"; 3  -> "خرداد"
    4  -> "تیر";     5  -> "مرداد";     6  -> "شهریور"
    7  -> "مهر";     8  -> "آبان";      9  -> "آذر"
    10 -> "دی";      11 -> "بهمن";      12 -> "اسفند"
    else -> ""
}

// ── الگوریتم تبدیل ───────────────────────────────────────

private fun isPersianLeapYear(jy: Int): Boolean {
    val breaks = intArrayOf(-61,9,38,199,426,686,756,818,1111,1181,1210,1635,2060,2097,2192,2262,2324,2394,2456,3178)
    var jp = breaks[0]
    var jump = 0
    for (i in 1 until breaks.size) {
        val jb = breaks[i]
        jump = jb - jp
        if (jy < jb) {
            val n = jy - jp
            val leapJ = if (jump == 366 || jump == 683 || jump == 518) {
                (n * 8 + 29) / 33
            } else {
                (n * 8 + 29) / 33
            }
            return (n - leapJ * 33 / 8) == 0
        }
        jp = jb
    }
    return false
}

private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): PersianDate {
    val gY = gy - 1600
    val gM = gm - 1
    val gD = gd - 1

    var gDNo = 365 * gY + (gY + 3) / 4 - (gY + 99) / 100 + (gY + 399) / 400
    val gMD = intArrayOf(31, if ((gy % 4 == 0 && gy % 100 != 0) || gy % 400 == 0) 29 else 28,
        31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    for (i in 0 until gM) gDNo += gMD[i]
    gDNo += gD

    var jDNo = gDNo - 79
    val jNp  = jDNo / 12053
    jDNo    %= 12053

    var jy   = 979 + 33 * jNp + 4 * (jDNo / 1461)
    jDNo    %= 1461

    if (jDNo >= 366) {
        jy   += (jDNo - 1) / 365
        jDNo  = (jDNo - 1) % 365
    }

    val jMD = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
    var jm = 12; var jd = jDNo + 1
    for (i in 0 until 11) {
        if (jDNo < jMD[i]) { jm = i + 1; jd = jDNo + 1; break }
        jDNo -= jMD[i]
    }
    return PersianDate(jy, jm, jd)
}

private fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): LocalDate {
    var jY = jy - 979
    val jM = jm - 1
    val jD = jd - 1

    var jDNo = 365 * jY + (jY / 33) * 8 + (jY % 33 + 3) / 4
    val jMD  = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
    for (i in 0 until jM) jDNo += jMD[i]
    jDNo += jD

    var gDNo = jDNo + 79
    var gy   = 1600 + 400 * (gDNo / 146097)
    gDNo    %= 146097

    var leap = true
    if (gDNo >= 36525) {
        gDNo -= 1
        gy   += 100 * (gDNo / 36524)
        gDNo %= 36524
        if (gDNo >= 365) gDNo += 1 else leap = false
    }

    gy   += 4 * (gDNo / 1461)
    gDNo %= 1461

    if (gDNo >= 366) {
        leap  = false
        gDNo -= 1
        gy   += gDNo / 365
        gDNo  = gDNo % 365
    }

    val gMD = intArrayOf(31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    var gm = 1; var gd = gDNo
    for (i in 0 until 12) {
        if (gd < gMD[i]) { gm = i + 1; gd += 1; break }
        gd -= gMD[i]
    }
    return LocalDate.of(gy, gm, gd)
}