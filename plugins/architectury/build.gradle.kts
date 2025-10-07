dependencies {
    api(project(":core"))

    api(libs.architectury.loom)

}

gradlePlugin {
    plugins {
        named(project.name) {
            implementationClass = "com.possible_triangle.gradle.architectury.GradleHelperArchitecturyPlugin"
        }
    }
}