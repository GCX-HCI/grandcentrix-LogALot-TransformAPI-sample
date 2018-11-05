package net.grandcentrix.gradle.logalot

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import org.amshove.kluent.shouldNotEqual
import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class LogALotPluginTest {

    @Test(expected = GradleException::class)
    fun `test that an error is thrown if not an Android project`() {
        val project = ProjectBuilder.builder()
            .build()

        val plugin = LogALotPlugin()
        plugin.apply(project)
    }

    @Test
    fun `test that the transformer is applied for Android apps`() {
        val project = ProjectBuilder.builder()
            .build()

        project.plugins.apply(AppPlugin::class.java)

        val plugin = LogALotPlugin()
        plugin.apply(project)

        val androidExtension = project.extensions.findByName("android") as BaseExtension
        androidExtension.transforms.find { it is LogALotTransformer } shouldNotEqual null

        project.extensions.findByName("logALot") shouldNotEqual null
    }

    @Test
    fun `test that the transformer is applied for Android libraries`() {
        val project = ProjectBuilder.builder()
            .build()

        project.plugins.apply(LibraryPlugin::class.java)

        val plugin = LogALotPlugin()
        plugin.apply(project)

        val androidExtension = project.extensions.findByName("android") as BaseExtension
        androidExtension.transforms.find { it is LogALotTransformer } shouldNotEqual null

        project.extensions.findByName("logALot") shouldNotEqual null
    }
}