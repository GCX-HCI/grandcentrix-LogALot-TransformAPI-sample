package net.grandcentrix.gradle.logalot

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.BaseExtension
import javassist.ClassPool
import javassist.CtClass
import javassist.expr.ExprEditor
import javassist.expr.FieldAccess
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.File
import java.lang.reflect.Modifier

/**
 * The transformer.
 */
class LogALotTransformer(
    private val project: Project,
    private val android: BaseExtension,
    private val extension: LogALotExtension,
    private val logger: Logger
) : Transform() {

    override fun getName(): String = "LogALot"

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> =
        setOf(QualifiedContent.DefaultContentType.CLASSES).toMutableSet()

    override fun isIncremental(): Boolean = false

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> =
        setOf(
            QualifiedContent.Scope.PROJECT,
            QualifiedContent.Scope.EXTERNAL_LIBRARIES,
            QualifiedContent.Scope.SUB_PROJECTS
        ).toMutableSet()

    @Suppress("ComplexMethod")
    override fun transform(transformInvocation: TransformInvocation) {
        if (extension.applyFor?.isEmpty() != false) {
            logger.warn("No variants to apply LogALot configured")
            return
        }

        val variant = transformInvocation.context.variantName
        if (extension.applyFor?.find { variant.endsWith(it, true) } == null) return

        val androidJar = "${android.sdkDirectory.absolutePath}/platforms/${android.compileSdkVersion}/android.jar"

        val externalDepsJars = mutableListOf<File>()
        val externalDepsDirs = mutableListOf<File>()

        transformInvocation.inputs.forEach { transformInput ->
            transformInput.jarInputs.forEach { externalDepsJars += it.file }
            transformInput.directoryInputs.forEach { externalDepsDirs += it.file }
        }

        val outputDir =
            transformInvocation.outputProvider.getContentLocation("classes", outputTypes, scopes, Format.DIRECTORY)
        outputDir.deleteRecursively()
        outputDir.mkdirs()

        externalDepsJars.forEach {
            val dst =
                transformInvocation.outputProvider.getContentLocation(it.absolutePath, outputTypes, scopes, Format.JAR)
            dst.delete()
            it.copyTo(dst)
        }
        externalDepsDirs.forEach {
            val dst =
                transformInvocation.outputProvider.getContentLocation(
                    it.absolutePath,
                    outputTypes,
                    scopes,
                    Format.DIRECTORY
                )
            dst.deleteRecursively()
            it.copyRecursively(outputDir)
        }

        transformInvocation.inputs.forEach { transformInput ->
            transformInput.directoryInputs.forEach { inputDirectory ->

                val baseDir = inputDirectory.file
                val pool = ClassPool()
                pool.appendSystemPath()
                pool.insertClassPath(baseDir.absolutePath)
                pool.insertClassPath(androidJar)
                externalDepsJars.forEach { pool.insertClassPath(it.absolutePath) }
                externalDepsDirs.forEach { pool.insertClassPath(it.absolutePath) }

                transformInput(inputDirectory, outputDir, pool)
            }
        }

    }

    private fun transformInput(
        inputDirectory: DirectoryInput,
        outputDir: File,
        pool: ClassPool
    ) {
        inputDirectory.file.walkTopDown().iterator().forEach { originalClassFile ->
            if (originalClassFile.isFile && originalClassFile.path.endsWith(".class")) {
                val classname =
                    originalClassFile.relativeTo(inputDirectory.file)
                        .path
                        .replace("/", ".")
                        .replace(".class", "")

                val clazz = pool.get(classname)
                transformClass(clazz)
                clazz.writeFile(outputDir.absolutePath)
            }
        }
    }

    // TODO use $proceed and $$ , see http://www.javassist.org/tutorial/tutorial2.html
    private fun transformClass(clazz: CtClass) {
        clazz.instrument(object : ExprEditor() {
            override fun edit(f: FieldAccess) {
                if (!f.field.hasAnnotation("net.grandcentrix.gradle.logalot.annotations.LogALot")) return

                if (f.isReader) {
                    f.replace(
                        """{
                            ${'$'}_ = ${'$'}0.${f.fieldName};
                            net.grandcentrix.gradle.logalot.runtime.LogALot.log("read field ${f.className}.${f.fieldName}, value is "+${'$'}_);
                        }"""
                    )
                } else {
                    f.replace(
                        """{
                            ${'$'}0.${f.fieldName} = ${'$'}1;
                            net.grandcentrix.gradle.logalot.runtime.LogALot.log("write field ${f.className}.${f.fieldName}, new value is "+${'$'}1);
                        }"""
                    )
                }
            }
        })

        clazz.declaredMethods.forEach { method ->
            val noBody = method.modifiers and Modifier.ABSTRACT == Modifier.ABSTRACT ||
                    method.modifiers and Modifier.NATIVE == Modifier.NATIVE
            if (!noBody && method.hasAnnotation("net.grandcentrix.gradle.logalot.annotations.LogALot")) {
                val params = Array<String>(method.parameterTypes.size) { "${'$'}${it + 1}" }.joinToString("""+","+""")
                if (params.isEmpty()) {
                    method.insertBefore(
                        """{net.grandcentrix.gradle.logalot.runtime.LogALot.log("${clazz.name}.${method.name}()");}"""
                    )
                } else {
                    method.insertBefore(
                        """{net.grandcentrix.gradle.logalot.runtime.LogALot.log("${clazz.name}.${method.name}("+$params+")");}"""
                    )
                }
            }
        }
    }

}