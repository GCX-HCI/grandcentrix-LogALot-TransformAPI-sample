plugins {
    kotlin("jvm") version "1.3.0"
    `java-gradle-plugin`
}

repositories {
    google()
    jcenter()
}

dependencies {
    implementation("com.android.tools.build:gradle:3.2.1")
    implementation("com.android.tools.build:gradle-api:3.2.1")
    implementation("org.javassist:javassist:3.23.1-GA")

    testImplementation("junit:junit:4.12")
    testImplementation("io.mockk:mockk:1.8.11")
    testImplementation("org.amshove.kluent:kluent:1.42")
    testImplementation("org.jboss.windup.decompiler.procyon:procyon-compilertools:2.5.0.Final")
}

gradlePlugin {
    plugins {
        create("logalot") {
            id = "net.grandcentrix.logalot"
            implementationClass = "net.grandcentrix.gradle.logalot.LogALotPlugin"
        }
    }
}