fun properties(key: String) = project.findProperty(key).toString()

plugins {
    idea
    // Java support
    id("java")
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij") version "1.13.1"
    id("maven-publish")
    id("distribution")
}

group = properties("pluginGroup")
version = properties("pluginVersion")
val sinceBuildVersion = properties("pluginSinceBuild")

// Configure project's dependencies
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.oracle.oci.sdk:oci-java-sdk-common-httpclient-jersey:3.24.0") {
        exclude(group="org.slf4j", module="slf4j-api")
    }
    implementation("com.oracle.oci.sdk:oci-java-sdk-common:3.24.0") {
        exclude(group="org.slf4j", module="slf4j-api")
    }
    implementation("com.oracle.oci.sdk:oci-java-sdk-core:3.24.0") {
        exclude(group="org.slf4j", module="slf4j-api")
    }
    implementation("com.oracle.oci.sdk:oci-java-sdk-database:3.24.0") {
        exclude(group="org.slf4j", module="slf4j-api")
    }
    implementation("com.oracle.oci.sdk:oci-java-sdk-identity:3.24.0") {
        exclude(group="org.slf4j", module="slf4j-api")
    }
    implementation("com.oracle.oci.sdk:oci-java-sdk-identitydataplane:3.24.0") {
        exclude(group="org.slf4j", module="slf4j-api")
    }
    implementation("com.oracle.oci.sdk:oci-java-sdk-resourcemanager:3.24.0") {
        exclude(group="org.slf4j", module="slf4j-api")
    }
    implementation("com.oracle.oci.sdk:oci-java-sdk-vault:3.24.0") {
        exclude(group="org.slf4j", module="slf4j-api")
    }
    implementation("com.oracle.oci.sdk:oci-java-sdk-keymanagement:3.24.0") {
        exclude(group="org.slf4j", module="slf4j-api")
    }

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0") // Use the latest version
    
    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("jakarta.json:jakarta.json-api:2.1.2")
    testImplementation("org.eclipse.parsson:parsson:1.1.5")
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
        systemProperties["idea.log.debug.categories"] = true
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

distributions {
  main {
    distributionBaseName.set("OCIPluginForIntelliJ.zip")
  }
}



//publishing {
//    publications {
//        maven(MavenPublication) {
//            groupId = "com.oracle.oci"
//            artifactId = "intellij.plugin"
//            version = "0.3-SNAPSHOT"
//
//            pom {
//                name = "My Library"
//                description = "A description of my library"
//            }
//        }
//    }

//    repositories {
//        maven {
//            credentials {
//                username = "jdbcmaventoolsso_us@oracle.com"
//                password = "AKCp8jQnLTKdt2DPJyJDv85u74rpyphECVUHw9uxmM1E8eyhk8msHXstPN1MvD6cwAk8YEuxA"
//            }
//
//            url = "https://artifacthub-phx.oci.oraclecorp.com/jdbc-dev-local"
//        }
//    }
//}
