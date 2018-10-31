package net.grandcentrix.gradle.logalot

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.BaseExtension
import javassist.ClassPool
import javassist.CtClass
import javassist.NotFoundException
import javassist.expr.ExprEditor
import javassist.expr.FieldAccess
import org.gradle.api.GradleException
import java.io.File

/**
 * The transformer.
 */
class LogALotTransformer(
    private val android: BaseExtension,
    private val extension: LogALotExtension
) : Transform() {

    override fun getName(): String = "LogALot"

    override fun getInputTypes(): Set<QualifiedContent.ContentType> = setOf(QualifiedContent.DefaultContentType.CLASSES)

    override fun isIncremental(): Boolean = false

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> =
        mutableSetOf(QualifiedContent.Scope.PROJECT)

    override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope> =
        mutableSetOf(
            QualifiedContent.Scope.EXTERNAL_LIBRARIES,
            QualifiedContent.Scope.SUB_PROJECTS
        )

    override fun transform(transformInvocation: TransformInvocation) {
        if (extension.applyFor?.isEmpty() != false) {
            throw GradleException("No variants to apply LogALot configured")
        }

        val variant = transformInvocation.context.variantName
        val applyTransform = extension.applyFor?.find { variant.endsWith(it, true) } != null

        val androidJar = "${android.sdkDirectory.absolutePath}/platforms/${android.compileSdkVersion}/android.jar"

        val externalDepsJars = mutableListOf<File>()
        val externalDepsDirs = mutableListOf<File>()

        transformInvocation.referencedInputs.forEach { transformInput ->
            externalDepsJars += transformInput.jarInputs.map { it.file }
            externalDepsDirs += transformInput.directoryInputs.map { it.file }
        }

        val outputDir =
            transformInvocation.outputProvider.getContentLocation("classes", outputTypes, scopes, Format.DIRECTORY)
        outputDir.deleteRecursively()
        outputDir.mkdirs()

        transformInvocation.inputs.forEach { transformInput ->
            transformInput.directoryInputs.forEach { inputDirectory ->

                val baseDir = inputDirectory.file
                val pool = ClassPool()
                pool.appendSystemPath()
                pool.insertClassPath(baseDir.absolutePath)
                pool.insertClassPath(androidJar)
                externalDepsJars.forEach { pool.insertClassPath(it.absolutePath) }
                externalDepsDirs.forEach { pool.insertClassPath(it.absolutePath) }

                if (applyTransform) {
                    try {
                        pool.get("net.grandcentrix.gradle.logalot.runtime.LogALot")
                    } catch (nfe: NotFoundException) {
                        throw GradleException(
                            "You have to add the runtime dependency at least for the variants you enabled LogALot for.",
                            nfe
                        )
                    }

                    transformInput(inputDirectory, outputDir, pool)
                } else {
                    inputDirectory.file.copyRecursively(outputDir, true)
                }
            }
        }

    }

    private fun transformInput(
        inputDirectory: DirectoryInput,
        outputDir: File,
        pool: ClassPool
    ) {
        inputDirectory.file.walkTopDown().forEach { originalClassFile ->
            if (originalClassFile.isFile && originalClassFile.path.endsWith(".class")) {
                val classname = originalClassFile.relativeTo(inputDirectory.file).toClassname()
                val clazz = pool.get(classname)
                transformClass(clazz)
                clazz.writeFile(outputDir.absolutePath)
            }
        }
    }

    private fun transformClass(clazz: CtClass) {
        clazz.instrument(object : ExprEditor() {
            override fun edit(f: FieldAccess) {
                if (!f.field.hasAnnotation("net.grandcentrix.gradle.logalot.annotations.LogALot")) return

                if (f.isReader) {
                    f.replace(
                        """{
                            @_ = @0.${f.fieldName};
                            net.grandcentrix.gradle.logalot.runtime.LogALot.logFieldRead("${f.className}","${f.fieldName}", "${clazz.name}", ${f.lineNumber}, (@w)@_);
                        }""".toJavassist()
                    )
                } else {
                    f.replace(
                        """{
                            @0.${f.fieldName} = @1;
                            net.grandcentrix.gradle.logalot.runtime.LogALot.logFieldWrite("${f.className}","${f.fieldName}", "${clazz.name}", ${f.lineNumber}, (@w)@1);
                        }""".toJavassist()
                    )
                }
            }
        })

        clazz.declaredMethods.forEach { method ->
            if (!method.isEmpty && method.hasAnnotation("net.grandcentrix.gradle.logalot.annotations.LogALot")) {
                method.insertBefore(
                    """{net.grandcentrix.gradle.logalot.runtime.LogALot.logMethodInvocation("${clazz.name}","${method.name}",@args);}""".toJavassist()
                )

            }
        }
    }
}

private fun String.toJavassist(): String = replace("@", "${'$'}")

private fun File.toClassname(): String =
    path.replace("/", ".")
        .replace("\\", ".")
        .replace(".class", "")
