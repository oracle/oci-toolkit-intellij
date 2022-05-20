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

// Configure project's dependencies
repositories {
    mavenCentral()
}

dependencies {
   implementation(files("lib/sdk/oci-java-sdk-full-2.13.1.jar"))
    implementation(fileTree("lib/thirdparty") { include("*.jar") })
    //fileTree("lib/sdk") { include("*.jar") })
/*    implementation(files("lib/sdk/oci-java-sdk-common-2.13.0.jar"))
    implementation(files("lib/sdk/oci-java-sdk-objectstorage-2.13.0.jar"))
    implementation(files("lib/sdk/oci-java-sdk-circuitbreaker-2.13.0.jar"))
    implementation(files("lib/sdk/oci-java-sdk-objectstorage-extensions-2.13.0.jar"))
    implementation(files("lib/sdk/oci-java-sdk-vault-2.13.0.jar"))
    implementation(files("lib/sdk/oci-java-sdk-core-2.13.0.jar"))
    implementation(files("lib/sdk/oci-java-sdk-keymanagement-2.13.0.jar"))
    implementation(files("lib/sdk/oci-java-sdk-database-2.13.0.jar"))
    implementation(files("lib/sdk/oci-java-sdk-workrequests-2.13.0.jar"))
    implementation(files("lib/sdk/oci-java-sdk-identity-2.13.0.jar"))
    implementation(files("lib/sdk/oci-java-sdk-secrets-2.13.0.jar"))
    implementation(files("lib/sdk/oci-java-sdk-objectstorage-generated-2.13.0.jar"))
    implementation(files("lib/sdk/oci-java-sdk-containerengine-2.13.0.jar"))
    */

    implementation(files("lib/thirdparty/resilience4j-circuitbreaker-1.2.0.jar"))

    //implementation("org.slf4j:org.slf4j-api:1.7.29")
    //implementation("com.oracle.oci.sdk:oci-java-sdk-common:2.17.0")
    //implementation("com.oracle.oci.sdk:oci-java-sdk-objectstorage:2.17.0")
    //implementation("com.oracle.oci.sdk:oci-java-sdk-circuitbreaker:2.17.0")
    //implementation("com.oracle.oci.sdk:oci-java-sdk-objectstorage-extensions:2.17.0")
    //implementation("com.oracle.oci.sdk:oci-java-sdk-vault:2.17.0")
    //implementation("com.oracle.oci.sdk:oci-java-sdk-core:2.17.0")
    //implementation("com.oracle.oci.sdk:oci-java-sdk-keymanagement:2.17.0")
    //implementation("com.oracle.oci.sdk:oci-java-sdk-database:2.17.0")
    //implementation("com.oracle.oci.sdk:oci-java-sdk-workrequests:2.17.0")
    //implementation("com.oracle.oci.sdk:oci-java-sdk-identity:2.17.0")
    //implementation("com.oracle.oci.sdk:oci-java-sdk-secrets:2.17.0")
    //implementation("com.oracle.oci.sdk:oci-java-sdk-objectstorage-generated:2.17.0")
    //implementation("com.oracle.oci.sdk:oci-java-sdk-containerengine:2.17.0")

    implementation("javax.ws.rs:javax.ws.rs-api:2.1")
    implementation("org.glassfish.jersey.core:jersey-client:2.27")
    implementation("org.glassfish.jersey.core:jersey-common:2.27")
    implementation("org.glassfish.jersey.inject:jersey-hk2:2.27")
    implementation("org.glassfish.jersey.media:jersey-media-json-jackson:2.27")
    implementation("org.glassfish.hk2:hk2-api:2.5.0-b42")
    implementation("org.glassfish.hk2:hk2-locator:2.5.0-b42")
    implementation("org.glassfish.hk2:hk2-utils:2.5.0-b4")

    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
    //downloadSources.set(!isCI)
//    sinceBuild.set(properties("pluginSinceBuild"))
//    untilBuild.set(properties("pluginUpdateBuild"))
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
}
