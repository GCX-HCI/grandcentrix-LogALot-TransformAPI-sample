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
}