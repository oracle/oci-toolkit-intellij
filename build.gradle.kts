//import org.jetbrains.changelog.markdownToHTML
//import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    idea
    // Java support
    id("java")
    // Kotlin support
    //id("org.jetbrains.kotlin.jvm") version "1.6.10"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij") version "1.4.0"
    // Gradle Changelog Plugin
    //id("org.jetbrains.changelog") version "1.3.1"
    // Gradle Qodana Plugin
    //id("org.jetbrains.qodana") version "0.1.13"
}

group = properties("pluginGroup")
version = properties("pluginVersion")
val sinceBuildVersion = properties("pluginSinceBuild")

// Configure project's dependencies
repositories {
    mavenCentral()
}

dependencies {
    implementation(files("lib/sdk/oci-java-sdk-full-2.34.0.jar"))
    implementation(fileTree("lib/thirdparty") { include("*.jar") })
    
    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
    //downloadSources.set(!isCI)
    updateSinceUntilBuild.set(false)
    //instrumentCode.set(false)
    //ideaDependencyCachePath.set(dependencyCachePath)
    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
    //sandboxDir.set("$buildDir/$baseIDE-sandbox-$platformVersion")
}


tasks {
    runIde {
        systemProperties["idea.auto.reload.plugins"] = true
        jvmArgs = listOf(
            "-Xms512m",
            "-Xmx2048m",
            "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=1044",
        )
    }

    patchPluginXml {
        version.set("${project.version}")
        sinceBuild.set(sinceBuildVersion)
    }
}
