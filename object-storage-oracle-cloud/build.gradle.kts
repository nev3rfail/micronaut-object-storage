plugins {
    io.micronaut.build.internal.`objectstorage-module`
}

dependencies {
    api(projects.micronautObjectStorageCore)
    api(mnOraclecloud.micronaut.oraclecloud.sdk) {
        exclude(group = "org.codehaus.groovy", module = "groovy")
    }
    api(mnOraclecloud.micronaut.oraclecloud.bmc.objectstorage)

    implementation(platform(mnOraclecloud.micronaut.oraclecloud.bom))

    testImplementation(mnValidation.micronaut.validation)
    testImplementation(mnValidation.micronaut.validation.processor)
    testImplementation(projects.micronautObjectStorageTck)
    testImplementation(libs.testcontainers.spock)
}
