package net.grandcentrix.logalot

import android.app.Application
import android.util.Log
import net.grandcentrix.gradle.logalot.runtime.logMethodInvocationDelegate

class DebugApp : Application() {

    companion object {
        init {
            logMethodInvocationDelegate = { clazz, method, params ->
                Log.d("LogALot", "Delegated method invocation log: $clazz $method ${params.joinToString(",")}")
            }
        }
    }
}