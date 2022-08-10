plugins {
    java
    `java-library`
    jacoco
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://maven.iais.fraunhofer.de/artifactory/eis-ids-public/")
        }
    }

    if (System.getenv("JACOCO") == "true") {
        apply(plugin = "jacoco")
        tasks.jacocoTestReport {
            reports {
                xml.required.set(true)
            }
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
        }
    }
}
