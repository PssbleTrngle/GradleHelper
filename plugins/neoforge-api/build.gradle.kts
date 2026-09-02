dependencies {
    api(project(":common"))
}

gradlePlugin {
    plugins {
        named(project.name) {
            implementationClass = "com.possible_triangle.gradle.neoforge_api.GradleHelperNeoForgeApiPlugin"
        }
    }
}
