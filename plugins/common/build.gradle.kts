dependencies {
    api(project(":core"))

    api(libs.neoforge.gradle.common)

}

gradlePlugin {
    plugins {
        named(project.name) {
            implementationClass = "com.possible_triangle.gradle.common.GradleHelperCommonPlugin"
        }
    }
}