@file:JvmName("LogALot")

package net.grandcentrix.gradle.logalot.runtime

/**
 * Actually logs.
 */
fun log(message: String) {
    println("Message is: $message")
}