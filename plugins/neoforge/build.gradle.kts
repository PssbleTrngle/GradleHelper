dependencies {
    api(project(":core"))
    api(project(":access"))

    api(libs.neoforge.gradle)
}

gradlePlugin {
    plugins {
        named(project.name) {
            implementationClass = "com.possible_triangle.gradle.neoforge.GradleHelperNeoForgePlugin"
        }
    }
}