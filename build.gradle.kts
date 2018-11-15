plugins {
    java
}

group = "io.rathr.audrey"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Truffle API
    implementation("org.graalvm.truffle:truffle-api:1.0.0-rc9")
    annotationProcessor("org.graalvm.truffle:truffle-dsl-processor:1.0.0-rc9")

    // JSON
    implementation("com.google.code.gson:gson:2.8.5")

    testImplementation("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}