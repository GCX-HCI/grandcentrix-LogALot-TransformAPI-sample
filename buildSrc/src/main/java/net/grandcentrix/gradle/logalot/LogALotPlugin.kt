package net.grandcentrix.gradle.logalot

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * The plugin.
 */
open class LogALotPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val isAndroid =
            !(target.plugins.withType(AppPlugin::class.java) + target.plugins.withType(LibraryPlugin::class.java)).isEmpty()
        if (!isAndroid) {
            throw GradleException("'com.android.application' or 'com.android.library' plugin required.")
        }

        val extension = target.extensions.create("logALot", LogALotExtension::class.java)

        @Suppress("UnsafeCast")
        val android = target.extensions.findByName("android") as BaseExtension
        android.registerTransform(LogALotTransformer(android, extension, target.logger))

        target.afterEvaluate {
            val shouldApplyTransform = gradle.startParameter.taskRequests.find { taskExecutionRequest ->
                taskExecutionRequest.args.find { arg ->
                    extension.applyFor?.find {
                        arg.endsWith(
                            it,
                            true
                        )
                    } != null
                } != null
            } != null

            // don't add dependency to runtime if we are not going to use it
            if (shouldApplyTransform) {
                target.dependencies.add("implementation", "net.grandcentrix.gradle.logalot:runtime:0.0.1")
            }
        }
    }

}

/**
 * The extenstion.
 */
open class LogALotExtension {
    var applyFor: Array<String>? = null
}