package net.grandcentrix.gradle.logalot

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.BaseExtension
import com.strobel.assembler.InputTypeLoader
import com.strobel.assembler.metadata.Buffer
import com.strobel.decompiler.Decompiler
import com.strobel.decompiler.DecompilerSettings
import com.strobel.decompiler.PlainTextOutput
import com.strobel.decompiler.languages.java.JavaFormattingOptions
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import javassist.ClassPath
import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod
import javassist.NotFoundException
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.annotation.Annotation
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContainAll
import org.junit.Test
import java.io.File
import java.io.StringWriter

class LogALotTransformerTest {

    private val baseExtension = mockk<BaseExtension>()
    private val extension = mockk<LogALotExtension>()
    private val transformer = LogALotTransformer(baseExtension, extension)

    @Test
    fun getName() {
        transformer.name shouldBeEqualTo "LogALot"
    }

    @Test
    fun getInputTypes() {
        transformer.inputTypes shouldContainAll listOf(QualifiedContent.DefaultContentType.CLASSES)
    }

    @Test
    fun isIncremental() {
        transformer.isIncremental shouldBe false
    }

    @Test
    fun getScopes() {
        transformer.scopes shouldContainAll listOf(QualifiedContent.Scope.PROJECT)
    }

    @Test
    fun getReferencedScopes() {
        transformer.referencedScopes shouldContainAll listOf(
            QualifiedContent.Scope.EXTERNAL_LIBRARIES,
            QualifiedContent.Scope.SUB_PROJECTS
        )
    }

    @Test
    fun `transform should not transform for a variant that's not configured`() {
        val sdkDir = mockk<File>()
        every { sdkDir.absolutePath } returns "/mysdkdir"

        every { extension.applyFor } returns arrayOf("debug")
        every { baseExtension.sdkDirectory } returns sdkDir
        every { baseExtension.compileSdkVersion } returns "26"

        val context = mockk<Context>()
        every { context.variantName } returns "Release"

        val referencedJarInputFile = mockk<File>()
        every { referencedJarInputFile.absolutePath } returns "/jars/jar.jar"
        val referencedJarInput = mockk<JarInput>()
        every { referencedJarInput.file } returns referencedJarInputFile
        val referencedJarInputs = listOf(referencedJarInput)

        val referencedDirectoryInputFile = mockk<File>()
        every { referencedDirectoryInputFile.absolutePath } returns "/directory"
        val referencedDirectoryInput = mockk<DirectoryInput>()
        every { referencedDirectoryInput.file } returns referencedDirectoryInputFile
        val referencedDirectoryInputs = listOf(referencedDirectoryInput)

        val referencedInput = mockk<TransformInput>()
        every { referencedInput.jarInputs } returns referencedJarInputs
        every { referencedInput.directoryInputs } returns referencedDirectoryInputs
        val referencedInputs = listOf(referencedInput)

        val contentLocation = mockk<File>()
        mockkStatic("kotlin.io.FilesKt__UtilsKt")
        every { contentLocation.deleteRecursively() } returns true
        every { contentLocation.mkdirs() } returns true

        val outputProvider = mockk<TransformOutputProvider>()
        every { outputProvider.getContentLocation("classes", any(), any(), Format.DIRECTORY) } returns contentLocation

        val directoryInputFile = mockk<File>()
        every { directoryInputFile.absolutePath } returns "/myinputdir"
        every { directoryInputFile.copyRecursively(any(), true) } returns true
        val directoryInput = mockk<DirectoryInput>()
        every { directoryInput.file } returns directoryInputFile
        val directoryInputs = listOf(directoryInput)
        val transformInput = mockk<TransformInput>()
        every { transformInput.directoryInputs } returns directoryInputs
        val transformInputs = listOf(transformInput)

        val transformInvocation = mockk<TransformInvocation>()
        every { transformInvocation.context } returns context
        every { transformInvocation.referencedInputs } returns referencedInputs
        every { transformInvocation.outputProvider } returns outputProvider
        every { transformInvocation.inputs } returns transformInputs

        val mockClassPath = mockk<ClassPath>()
        mockkConstructor(ClassPool::class)
        every { anyConstructed<ClassPool>().insertClassPath(any<String>()) } returns mockClassPath

        transformer.transform(transformInvocation)

        verify { directoryInputFile.copyRecursively(contentLocation, true) }
    }

    @Test
    fun `transform should transform for a variant that's configured`() {
        val sdkDir = mockk<File>()
        every { sdkDir.absolutePath } returns "/mysdkdir"

        every { extension.applyFor } returns arrayOf("debug")
        every { baseExtension.sdkDirectory } returns sdkDir
        every { baseExtension.compileSdkVersion } returns "26"

        val context = mockk<Context>()
        every { context.variantName } returns "Debug"

        val referencedJarInputFile = mockk<File>()
        every { referencedJarInputFile.absolutePath } returns "/jars/jar.jar"
        val referencedJarInput = mockk<JarInput>()
        every { referencedJarInput.file } returns referencedJarInputFile
        val referencedJarInputs = listOf(referencedJarInput)

        val referencedDirectoryInputFile = mockk<File>()
        every { referencedDirectoryInputFile.absolutePath } returns "/directory"
        val referencedDirectoryInput = mockk<DirectoryInput>()
        every { referencedDirectoryInput.file } returns referencedDirectoryInputFile
        val referencedDirectoryInputs = listOf(referencedDirectoryInput)

        val referencedInput = mockk<TransformInput>()
        every { referencedInput.jarInputs } returns referencedJarInputs
        every { referencedInput.directoryInputs } returns referencedDirectoryInputs
        val referencedInputs = listOf(referencedInput)

        val contentLocation = mockk<File>()
        mockkStatic("kotlin.io.FilesKt__UtilsKt")
        every { contentLocation.deleteRecursively() } returns true
        every { contentLocation.mkdirs() } returns true
        every { contentLocation.absolutePath } returns "/output"

        val outputProvider = mockk<TransformOutputProvider>()
        every { outputProvider.getContentLocation("classes", any(), any(), Format.DIRECTORY) } returns contentLocation

        val classfile = mockk<File>()
        every { classfile.path } returns "/myinputdir/pkg/Input.class"
        every { classfile.isFile } returns true
        val inputFilesSequence = listOf(classfile)
        val inputFiles = mockk<FileTreeWalk>()
        every { inputFiles.iterator() } returns inputFilesSequence.iterator()

        val directoryInputFile = mockk<File>()
        every { directoryInputFile.absolutePath } returns "/myinputdir"
        every { directoryInputFile.path } returns "/myinputdir"
        every { directoryInputFile.copyRecursively(any(), true) } returns true
        mockkStatic("kotlin.io.FilesKt__FileTreeWalkKt")
        every { directoryInputFile.walkTopDown() } returns inputFiles
        val directoryInput = mockk<DirectoryInput>()
        every { directoryInput.file } returns directoryInputFile
        val directoryInputs = listOf(directoryInput)
        val transformInput = mockk<TransformInput>()
        every { transformInput.directoryInputs } returns directoryInputs
        val transformInputs = listOf(transformInput)


        val transformInvocation = mockk<TransformInvocation>()
        every { transformInvocation.context } returns context
        every { transformInvocation.referencedInputs } returns referencedInputs
        every { transformInvocation.outputProvider } returns outputProvider
        every { transformInvocation.inputs } returns transformInputs

        val originalClassPool = ClassPool()
        originalClassPool.appendSystemPath()
        val runtimeClazz = originalClassPool.makeClass("net.grandcentrix.gradle.logalot.runtime.LogALot")
        runtimeClazz.addMethod(
            CtMethod.make(
                "public static void logMethodInvocation(String a, String b, Object o){}",
                runtimeClazz
            )
        )
        runtimeClazz.addMethod(
            CtMethod.make(
                "public static void logMethodExit(String a, String b, boolean bool, Object o){}",
                runtimeClazz
            )
        )
        runtimeClazz.addMethod(
            CtMethod.make(
                "public static void logMethodThrows(String a, String b, Throwable o){}",
                runtimeClazz
            )
        )
        runtimeClazz.addMethod(
            CtMethod.make(
                "public static void logFieldRead(String a, String b, String c, int d, Object o){}",
                runtimeClazz
            )
        )
        runtimeClazz.addMethod(
            CtMethod.make(
                "public static void logFieldWrite(String a, String b, String c, int d, Object o){}",
                runtimeClazz
            )
        )

        // the annotation isn't needed to be present as a class in the pool

        val inputClazz = originalClassPool.makeClass("pkg.Input")
        inputClazz.addField(CtField.make("public int myfield = 0;", inputClazz))
        inputClazz.addMethod(CtMethod.make("public void something(){ myfield = myfield + 1; }", inputClazz))

        val annotationAttribute =
            inputClazz.makeAnnotation("net.grandcentrix.gradle.logalot.annotations.LogALot")
        inputClazz.methods.forEach { it.methodInfo.addAttribute(annotationAttribute) }
        inputClazz.fields.forEach { it.fieldInfo.addAttribute(annotationAttribute) }

        val writtenClazzes = mutableMapOf<String, CtClass>()
        val mockClassPath = mockk<ClassPath>()
        mockkConstructor(ClassPool::class)
        every { anyConstructed<ClassPool>().insertClassPath(any<String>()) } returns mockClassPath
        every { anyConstructed<ClassPool>().get(any<String>()) } answers {
            val originalClazzSpy = spyk(originalClassPool.get(it.invocation.args[0] as String))
            every { originalClazzSpy.writeFile(any()) } returns Unit
            writtenClazzes += originalClazzSpy.name to originalClazzSpy
            originalClazzSpy
        }

        transformer.transform(transformInvocation)

        verify(exactly = 0) { directoryInputFile.copyRecursively(contentLocation, true) }
        writtenClazzes.size shouldBe 2

        val decompiledClazz = decompile("pkg.Input", originalClassPool)
        decompiledClazz.removeWhitespaces() shouldBeEqualTo """
            package pkg;

            import net.grandcentrix.gradle.logalot.annotations.*;

            public class Input
            {
                @LogALot
                public int myfield;

                @LogALot
                public void something() {
                    try {
                        net.grandcentrix.gradle.logalot.runtime.LogALot.logMethodInvocation("pkg.Input", "something", new Object[0]);
                        final int myfield = this.myfield;
                        net.grandcentrix.gradle.logalot.runtime.LogALot.logFieldRead("pkg.Input", "myfield", "pkg.Input", -1, new Integer(myfield));
                        final int myfield2 = myfield + 1;
                        this.myfield = myfield2;
                        net.grandcentrix.gradle.logalot.runtime.LogALot.logFieldWrite("pkg.Input", "myfield", "pkg.Input", -1, new Integer(myfield2));
                        net.grandcentrix.gradle.logalot.runtime.LogALot.logMethodExit("pkg.Input", "something", true, null);
                    }
                    catch (Throwable t) {
                        net.grandcentrix.gradle.logalot.runtime.LogALot.logMethodThrows("pkg.Input", "something", t);
                        throw t;
                    }
                }

                public Input() {
                    this.myfield = 0;
                }
            }
        """.removeWhitespaces()
    }
}

private fun decompile(className: String, classPool: ClassPool): String {
    val settings = DecompilerSettings().apply {
        formattingOptions = JavaFormattingOptions.createDefault()
        typeLoader = JavassistTypeLoader(classPool)
    }

    val stringWriter = StringWriter()
    Decompiler.decompile(className.replace(".", "/"), PlainTextOutput(stringWriter), settings)
    stringWriter.flush()
    return stringWriter.toString()
}

private fun CtClass.makeAnnotation(annotationName: String): AnnotationsAttribute {
    val classFile = classFile
    val cPool = classFile.constPool
    val annotationAttribute = AnnotationsAttribute(cPool, AnnotationsAttribute.visibleTag)
    val annotation = Annotation(annotationName, cPool)
    annotationAttribute.addAnnotation(annotation)
    return annotationAttribute
}

private fun String.removeWhitespaces(): String = replace(" ", "").replace("\n", "").replace("\r", "")

private class JavassistTypeLoader(val classPool: ClassPool) : InputTypeLoader() {
    val classBytesCache = mutableMapOf<String, ByteArray>()

    override fun tryLoadType(typeNameOrPath: String, buffer: Buffer): Boolean {
        try {
            val className = typeNameOrPath.replace("/", ".")
            val bytes = classBytesCache[className] ?: classPool[className].toBytecode()
            classBytesCache[className] = bytes
            buffer.reset(bytes.size)
            bytes.forEach { buffer.writeByte(it.toInt()) }
            buffer.position(0)
        } catch (nfe: NotFoundException) {
            return false
        }
        return true
    }
}