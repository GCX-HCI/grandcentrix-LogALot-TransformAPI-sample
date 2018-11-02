@file:JvmName("LogALot")

package net.grandcentrix.gradle.logalot.runtime

import android.util.Log

var logMethodInvocationDelegate: ((clazz: String, methodName: String, parameters: Array<Any?>) -> Unit)? =
    ::defaultLogMethodInvocation
var logMethodExitDelegate: ((clazz: String, methodName: String, resultIsVoid: Boolean, result: Any?) -> Unit)? =
    ::defaultLogMethodExit
var logMethodThrowsDelegate: ((clazz: String, methodName: String, throwable: Throwable) -> Unit)? =
    ::defaultLogMethodThrows
var logFieldWriteDelegate: ((clazz: String, fieldName: String, where: String, whereLine: Int, value: Any?) -> Unit)? =
    ::defaultLogFieldWrite
var logFieldReadDelegate: ((clazz: String, fieldName: String, where: String, whereLine: Int, value: Any) -> Unit)? =
    ::defaultLogFieldRead

/**
 * Log method invocation.
 */
fun logMethodInvocation(clazz: String, methodName: String, parameters: Array<Any?>) =
    logMethodInvocationDelegate?.invoke(clazz, methodName, parameters)

/**
 * Log method exit.
 */
fun logMethodExit(clazz: String, methodName: String, resultIsVoid: Boolean, result: Any?) =
    logMethodExitDelegate?.invoke(clazz, methodName, resultIsVoid, result)

/**
 * Log method exit with exception.
 */
fun logMethodThrows(clazz: String, methodName: String, throwable: Throwable) =
    logMethodThrowsDelegate?.invoke(clazz, methodName, throwable)

/**
 * Log field write.
 */
fun logFieldWrite(clazz: String, fieldName: String, where: String, whereLine: Int, value: Any?) =
    logFieldWriteDelegate?.invoke(clazz, fieldName, where, whereLine, value)

/**
 * Log field read.
 */
fun logFieldRead(clazz: String, fieldName: String, where: String, whereLine: Int, value: Any) =
    logFieldReadDelegate?.invoke(clazz, fieldName, where, whereLine, value)

// default implementations
private fun defaultLogMethodInvocation(clazz: String, methodName: String, parameters: Array<Any?>) {
    if (parameters.isEmpty()) {
        Log.d(TAG, "Invoked $clazz.$methodName")
    } else {
        Log.d(TAG, "Invoked $clazz.$methodName with parameters: ${parameters.joinToString(",")}")
    }
}

private fun defaultLogMethodExit(clazz: String, methodName: String, resultIsVoid: Boolean, result: Any?) {
    if (!resultIsVoid && result != null) {
        Log.d(TAG, "Exiting $clazz.$methodName with result: $result")
    } else {
        Log.d(TAG, "Exiting $clazz.$methodName without result")
    }
}

private fun defaultLogMethodThrows(clazz: String, methodName: String, throwable: Throwable) {
    Log.d(TAG, "Exiting $clazz.$methodName abnormally with $throwable")
}

private fun defaultLogFieldWrite(clazz: String, fieldName: String, where: String, whereLine: Int, value: Any?) {
    Log.d(TAG, "write field $clazz.$fieldName in $where:$whereLine values is $value")
}

private fun defaultLogFieldRead(clazz: String, fieldName: String, where: String, whereLine: Int, value: Any) {
    Log.d(TAG, "read field $clazz.$fieldName in $where:$whereLine values is $value")
}

private const val TAG = "LogALot"