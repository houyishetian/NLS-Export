package com.lin.nls_export.utils

fun Long.toSecondString(): String {
    val mill = this % 1000
    val second = this / 1000
    return "$second.$mill"
}