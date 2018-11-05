plugins {
    `kotlin-dsl`
}

repositories {
    google()
    jcenter()
    mavenCentral()
}


dependencies {
    compile("com.android.tools.build:gradle:3.2.1")
    compile("com.android.tools.build:gradle-api:3.2.1")
    compile("org.javassist:javassist:3.23.1-GA")

    testCompile("junit:junit:4.12")
    testImplementation("io.mockk:mockk:1.8.11")
    testImplementation("org.amshove.kluent:kluent:1.42")
    testImplementation("org.jboss.windup.decompiler.procyon:procyon-compilertools:2.5.0.Final")
}