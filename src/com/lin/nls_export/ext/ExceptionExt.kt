package com.lin.nls_export.ext

fun <T> tryCatchAllExceptions(block: () -> T, exceptionValue: T? = null): T? {
    return try {
        block.invoke()
    } catch (e: Exception) {
        e.printException()
        exceptionValue
    }
}

val enableExceptionPrint = false
fun Exception.printException() {
    if (enableExceptionPrint) {
        this.printStackTrace()
    }
}