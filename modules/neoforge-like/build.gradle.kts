plugins {
    `kotlin-dsl`
    publishing
}

dependencies {
    api(project(":core"))
    implementation(libs.neoforge.gradle)
}
