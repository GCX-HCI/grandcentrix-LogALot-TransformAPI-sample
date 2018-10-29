package net.grandcentrix.gradle.logalot.annotations

/**
 * Use this annotation to have the method or field logged.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
annotation class LogALot
