plugins {
    java
    id("jacoco")
    id("com.github.kt3k.coveralls") version "2.8.2"
    id("com.github.johnrengelman.shadow") version "4.0.3"
}

group = "io.rathr.audrey"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val graalvmVersion = "rc14"

    // Truffle API
    implementation("org.graalvm.truffle:truffle-api:1.0.0-$graalvmVersion")
    implementation("org.graalvm.truffle:truffle-dsl-processor:1.0.0-$graalvmVersion")

    // JSON
    implementation("com.google.code.gson:gson:2.8.5")

    // Redis Client
    implementation("io.lettuce:lettuce-core:5.1.3.RELEASE")

    // LSP
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.6.0")

    // For parsing JS
    implementation("org.graalvm.js:js:1.0.0-$graalvmVersion")

    // For parsing Ruby
    implementation(files("libs/jrubyparser-0.5.5-SNAPSHOT.jar"))

    testImplementation("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

task("install") {
    group = "Build"
    description = "Places an Audrey fat JAR into \$JAVA_HOME/jre/tools/."

    dependsOn("shadowJar")

    val graalvmHome = System.getenv("JAVA_HOME")
    mkdir("$graalvmHome/jre/tools/audrey")

    doLast {
        copy {
            from("build/libs/")
            into("$graalvmHome/jre/tools/audrey/")
        }
    }
}

task("uninstall") {
    group = "Build"
    description = "Removes the Audrey fat JAR from \$JAVA_HOME/jre/tools/."

    val graalvmHome = System.getenv("JAVA_HOME")
    doLast {
        delete("$graalvmHome/jre/tools/audrey")
    }
}

task("startServer", JavaExec::class) {
    description = "Starts the Audrey language server"
    classpath = java.sourceSets["main"].runtimeClasspath
    main = "io.rathr.audrey.lsp.AudreyServer"
}

tasks.withType<Test> {
    // Ensure that there's no Audrey fat JAR in the tools folder, because we want to load this build.
    dependsOn("uninstall")

    outputs.upToDateWhen { false }
    jvmArgs("-XX:-UseJVMCIClassLoader")
}

tasks.withType<JavaExec> {
    jvmArgs("-XX:-UseJVMCIClassLoader")
}

tasks {
    getByName<JacocoReport>("jacocoTestReport") {
        reports {
            // Coveralls wants this XML
            xml.isEnabled = true
        }
    }
}
