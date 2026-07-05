package com.mnfarzaneh.taskflow.utils

fun Int.toEnglishDigits(): String = this.toString()
fun Long.toEnglishDigits(): String = this.toString()

//// یه extension function بساز — هر جای پروژه قابل استفاده
//fun Int.toEnglishDigits(): String {
//    return this.toString()
//        .replace('۰', '0').replace('۱', '1').replace('۲', '2')
//        .replace('۳', '3').replace('۴', '4').replace('۵', '5')
//        .replace('۶', '6').replace('۷', '7').replace('۸', '8')
//        .replace('۹', '9')
//}