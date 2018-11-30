plugins {
    java
    id("com.github.johnrengelman.shadow") version "4.0.3"
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

    // Redis Client
    implementation("io.lettuce:lettuce-core:5.1.3.RELEASE")

    testImplementation("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

task("install") {
    group = "Build"
    description = "Installs a fat JAR into \$GRAALVM/jre/tools/."

    dependsOn("shadowJar")

    val graalvmHome = System.getenv("GRAALVM")
    mkdir("$graalvmHome/jre/tools/audrey")

    doLast {
        copy {
            from("build/libs/")
            into("$graalvmHome/jre/tools/audrey/")
        }
    }
}
