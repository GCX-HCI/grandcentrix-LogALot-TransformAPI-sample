@file:JvmName("LogALot")

package net.grandcentrix.gradle.logalot.runtime

import android.util.Log

/**
 * Log method invocation.
 */
fun logMethodInvocation(clazz: String, methodName: String, vararg parameters: Any) {
    Log.d("LogALot", "Invoked $clazz.$methodName with parameters: ${parameters.joinToString(",")}")
}

/**
 * Log field write.
 */
fun logFieldWrite(clazz: String, fieldName: String, where: String, whereLine: Int, value: Any) {
    Log.d("LogALot", "write field $clazz.$fieldName in $where:$whereLine values is $value")
}

/**
 * Log field read.
 */
fun logFieldRead(clazz: String, fieldName: String, where: String, whereLine: Int, value: Any) {
    Log.d("LogALot", "read field $clazz.$fieldName in $where:$whereLine values is $value")
}
