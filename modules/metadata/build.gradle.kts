plugins {
    `kotlin-dsl`
    publishing
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.kotlin.serialization.xml)

    testImplementation(libs.bundles.testing)
}
