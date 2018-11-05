package net.grandcentrix.gradle.logalot.annotations

/**
 * The annotation used by the test code // would be nicer to dynamically create it during the test
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
annotation class LogALot