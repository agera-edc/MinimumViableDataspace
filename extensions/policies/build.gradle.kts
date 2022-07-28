plugins {
    `java-library`
}

val edcVersion: String by project
val edcGroup: String by project
val jupiterVersion: String by project
val assertj: String by project

dependencies {
    api("${edcGroup}:ids-spi:${edcVersion}")
    api("${edcGroup}:contract-spi:${edcVersion}")
    api("${edcGroup}:core-base:${edcVersion}")
    implementation("${edcGroup}.identityhub:identity-hub-spi:${edcVersion}")

    testImplementation("org.assertj:assertj-core:${assertj}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")
}
