@file:JvmName("LogALot")

package net.grandcentrix.gradle.logalot.runtime

import android.util.Log

/**
 * Log method invocation.
 */
fun logMethodInvocation(clazz: String, methodName: String, vararg parameters: Any?) {
    if (parameters.isEmpty()) {
        Log.d(TAG, "Invoked $clazz.$methodName")
    } else {
        Log.d(TAG, "Invoked $clazz.$methodName with parameters: ${parameters.joinToString(",")}")
    }
}

/**
 * Log method exit.
 */
fun logMethodExit(clazz: String, methodName: String, resultIsVoid: Boolean, result: Any?) {
    if (!resultIsVoid && result != null) {
        Log.d(TAG, "Exiting $clazz.$methodName with result: $result")
    } else {
        Log.d(TAG, "Exiting $clazz.$methodName without result")
    }
}

/**
 * Log method exit with exception.
 */
fun logMethodThrows(clazz: String, methodName: String, throwable: Throwable) {
    Log.d(TAG, "Exiting $clazz.$methodName abnormally with $throwable")
}

/**
 * Log field write.
 */
fun logFieldWrite(clazz: String, fieldName: String, where: String, whereLine: Int, value: Any?) {
    Log.d(TAG, "write field $clazz.$fieldName in $where:$whereLine values is $value")
}

/**
 * Log field read.
 */
fun logFieldRead(clazz: String, fieldName: String, where: String, whereLine: Int, value: Any) {
    Log.d(TAG, "read field $clazz.$fieldName in $where:$whereLine values is $value")
}

private const val TAG = "LogALot"