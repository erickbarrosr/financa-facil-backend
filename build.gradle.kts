plugins {
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
    id("jacoco")
    id("checkstyle")
    id("com.github.spotbugs") version "6.0.26"
}

group = "com.financafacil"
version = "0.0.1-SNAPSHOT"

java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
}

configurations {
    compileOnly { extendsFrom(configurations.annotationProcessor.get()) }
}

repositories {
    mavenCentral()
}

val jjwtVersion = "0.12.6"
val mapstructVersion = "1.6.3"
val lombokMapstructBindingVersion = "0.2.0"
val testcontainersVersion = "1.21.3"
val restAssuredVersion = "5.5.0"
val springdocVersion = "2.6.0"

// Override Spring Boot BOM's Testcontainers version (1.19.8) with 1.20.4
// 1.19.8 uses Docker API 1.32 which is rejected by Docker daemons requiring 1.44+
dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:$testcontainersVersion")
    }
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Database
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    // MapStruct + Lombok
    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:$lombokMapstructBindingVersion")

    // Hypersistence Utils (JSONB support)
    implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.8.3")

    // OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")

    // Observability
    implementation("io.micrometer:micrometer-registry-prometheus")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    testImplementation("io.rest-assured:spring-mock-mvc:$restAssuredVersion")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

// ──────────────────────────────────────────────────────────────────────────────
// Docker API version proxy
//
// Docker 29.x enforces a minimum API version of 1.44, but Testcontainers' shaded
// docker-java library hard-codes "v1.32" in every HTTP request URL (e.g.
// /v1.32/containers/create). Docker rejects those requests with HTTP 400.
//
// Workaround: a lightweight Python proxy (config/docker-proxy.py) listens on a
// Unix socket at /tmp/docker-proxy.sock and rewrites every "/v1.XX/" URL
// segment to "/v1.44/" before forwarding to /var/run/docker.sock.
//
// The proxy is started automatically before the test task and terminated after.
// DOCKER_HOST points Testcontainers to the proxy socket.
// TESTCONTAINERS_RYUK_DISABLED=true keeps the reaper off the proxy path.
// ──────────────────────────────────────────────────────────────────────────────
var dockerProxyProcess: Process? = null

tasks.withType<Test> {
    useJUnitPlatform()
    environment("DOCKER_HOST", "unix:///tmp/docker-proxy.sock")
    environment("TESTCONTAINERS_RYUK_DISABLED", "true")

    doFirst {
        val proxySocket = file("/tmp/docker-proxy.sock")
        if (proxySocket.exists()) proxySocket.delete()
        val proxyScript = file("config/docker-proxy.py")
        dockerProxyProcess = ProcessBuilder("python3", proxyScript.absolutePath)
            .redirectOutput(file("/tmp/docker-proxy.log"))
            .redirectErrorStream(true)
            .start()
        // Give the proxy a moment to bind the socket
        Thread.sleep(600)
    }

    doLast {
        dockerProxyProcess?.destroy()
        file("/tmp/docker-proxy.sock").delete()
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

checkstyle {
    toolVersion = "10.18.2"
    configFile = file("config/checkstyle/checkstyle.xml")
}
