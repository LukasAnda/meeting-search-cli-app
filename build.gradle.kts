allprojects {
    repositories {
        jcenter()
        mavenCentral()
        maven("https://dl.bintray.com/kotlin/kotlinx/")
    }
}

subprojects {
    tasks.register<Copy>("packageDistribution") {
        dependsOn("jar")
        from("${project.rootDir}/scripts/meetings")
        from("${project.projectDir}/build/libs/${project.name}.jar") {
            into("lib")
        }
        into("${project.rootDir}/dist")
    }
}
