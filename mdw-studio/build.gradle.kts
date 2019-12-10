import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.intellij.tasks.PublishTask
import org.jetbrains.intellij.tasks.RunIdeTask

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.61"
    id("org.jetbrains.intellij") version "0.4.9"
}

group = "com.centurylink.mdw"
version = "1.3.8-SNAPSHOT"

sourceSets.main {
    withConvention(KotlinSourceSet::class) {
        kotlin.srcDirs("src")
        resources.srcDirs("resources")
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    maven { url = uri("https://www.jetbrains.com/intellij-repository/snapshots") }
}

dependencies {
    compile(project(":mdw-draw"))
    compile("com.beust:jcommander:1.72")
    compile("org.eclipse.jgit:org.eclipse.jgit:4.8.0.201706111038-r") { isTransitive = false }
    compile("org.yaml:snakeyaml:1.18")
    compile(files("lib/bpmn-schemas.jar"))
    compile("io.swagger:swagger-codegen-cli:2.3.1") { exclude(group = "org.slf4j") }
    compileOnly(files("${System.getProperty("java.home")}/../lib/tools.jar"))
}

intellij {
    version = "2019.3" // or like "192.5728-EAP-CANDIDATE-SNAPSHOT"
    setPlugins("Kotlin","java","git4idea")
}

tasks.withType<RunIdeTask> {
    jvmArgs = listOf("-Xmx1G") //, "-XX:CICompilerCount=2")
    // available jbrs here: https://jetbrains.bintray.com/intellij-jdk/
    // jbrVersion("11_0_2b164")
}

tasks.withType<PublishTask> {
    username(project.properties["intellijPublishUsername"] ?: "")
    password(project.properties["intellijPublishPassword"] ?: "")
    channels(project.properties["intellijPublishChannel"] ?: "EAP")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}