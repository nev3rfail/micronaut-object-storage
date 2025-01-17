plugins {
    io.micronaut.build.internal.`objectstorage-module`
}

dependencies {
    api(projects.micronautObjectStorageCore)
    api(mnGcp.micronaut.gcp.common)

    api(platform(libs.gcp.libraries.bom))
    api(libs.gcp.storage)

    implementation(platform(mnGcp.micronaut.gcp.bom))

    testImplementation(mnValidation.micronaut.validation)
    testImplementation(mnValidation.micronaut.validation.processor)
    testImplementation(projects.micronautObjectStorageTck)
    testImplementation(libs.testcontainers.spock)
}
