dependencies {
    api(project(":core"))

    api(libs.vanilla.gradle)

}

gradlePlugin {
    plugins {
        named(project.name) {
            implementationClass = "com.possible_triangle.gradle.vanilla.GradleHelperVanillaPlugin"
        }
    }
}