import nu.studer.gradle.jooq.JooqGenerate
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Logging
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile

/*
 * MyIAM 認証サービス - ビルド構成定義 (Gradle Kotlin DSL)
 *
 * ※バージョン番号などの定義はすべて gradle/libs.versions.toml で管理する。
 */

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        // jooq コード生成用の使い捨て PostgreSQL を起動する
        classpath(libs.testcontainers.postgresql)
    }
}

plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.jooq)
}

group = "com.myiam"
version = "0.0.1-SNAPSHOT"

// Boot BOM が管理する jooq の版をカタログの版で上書きする
extra["jooq.version"] = libs.versions.jooq.get()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom(libs.spring.modulith.bom.get().toString())
    }
}

// -----------------------------------------------------------------------------
// source set
//   src/test            : 単体（純 JVM、Docker 不要）
//   src/integrationTest : 統合（Spring コンテキスト + Testcontainers、Docker 必須）
//   ※ dependencies より前に定義すること
// -----------------------------------------------------------------------------
sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    }
}

configurations["integrationTestImplementation"].extendsFrom(configurations.testImplementation.get())
configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

// -----------------------------------------------------------------------------
// 依存関係
// -----------------------------------------------------------------------------
dependencies {
    // --- Spring Boot / Modulith ---
    implementation(libs.bundles.spring.boot.web)
    implementation(libs.bundles.spring.boot.data)
    implementation(libs.bundles.spring.boot.oauth2)
    implementation(libs.bundles.spring.modulith)

    // --- データベース ---
    runtimeOnly(libs.postgresql)
    jooqGenerator(libs.postgresql)
    implementation(libs.jooq.jackson3.extensions)

    // --- コード生成（Lombok / MapStruct） ---
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.mapstruct)
    annotationProcessor(libs.mapstruct.processor)
    annotationProcessor(libs.lombok.mapstruct.binding)

    // --- ユーティリティ ---
    implementation(libs.caffeine)
    implementation(libs.guava)
    implementation(libs.jmolecules.ddd)

    // --- テスト：単体 ---
    testImplementation(libs.spring.boot.starter.test)
    testRuntimeOnly(libs.junit.platform.launcher)

    // --- テスト：統合 ---
    "integrationTestImplementation"(libs.bundles.integration.test)
}

// -----------------------------------------------------------------------------
// テスト task
//   ./gradlew test             → 単体のみ
//   ./gradlew integrationTest  → 統合のみ
//   ./gradlew check / build    → 両方
// -----------------------------------------------------------------------------
tasks.named<Test>("test") {
    useJUnitPlatform()
}

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "統合テストを実行する（Docker 必須）"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks.test)
}

tasks.check { dependsOn(integrationTest) }

// -----------------------------------------------------------------------------
// jooq コード生成
//   スキーマの単一真相は docker/postgres/init/*.sql。
//   生成時に Testcontainers で使い捨ての PostgreSQL を起動し、その SQL を流してから生成する。
//   → compose の DB を起動していなくてもビルドできる（Docker は必要）
// -----------------------------------------------------------------------------
val jooqSchemaDir = layout.projectDirectory.dir("docker/postgres/init")

jooq {
    version.set(libs.versions.jooq.get())

    configurations {
        create("main") {
            jooqConfiguration.apply {
                logging = Logging.INFO

                // 接続情報 ※url は generateJooq 実行時にコンテナのものへ差し替える
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    user = "user"
                    password = "password"
                }

                generator.apply {
                    name = "org.jooq.codegen.JavaGenerator"

                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        // 全ユーザースキーマを対象、システム系は除外
                        includes = ".*"
                        excludes = "(information_schema|pg_catalog|pg_toast|pg_temp_.*|pg_toast_temp_.*)\\..*|" +
                                   "pgp_.*|armor|dearmor|crypt|digest|hmac|gen_salt|gen_random_bytes|gen_random_uuid"
                        // 楽観ロック用カラム
                        recordVersionFields = "version"
                        // 型の強制変換：timestamptz → Instant、jsonb → JsonNode
                        forcedTypes.addAll(listOf(
                            ForcedType().apply {
                                name = "INSTANT"
                                includeTypes = "(?i)timestamptz|timestamp\\ with\\ time\\ zone"
                            },
                            ForcedType().apply {
                                userType = "tools.jackson.databind.JsonNode"
                                isJsonConverter = true
                                includeTypes = "jsonb"
                            }
                        ))
                    }

                    generate.apply {
                        isFluentSetters = true
                        isJavaTimeTypes = true
                    }

                    target.apply {
                        packageName = "com.myiam.jooq"
                    }
                }
            }
        }
    }
}

tasks.named<JooqGenerate>("generateJooq") {
    // スキーマ SQL が変わったら再生成する
    inputs.dir(jooqSchemaDir)

    // 使い捨て PostgreSQL（docker compose と同じイメージ・同じ init SQL）
    val container = PostgreSQLContainer(DockerImageName.parse("postgres:18-alpine"))
        .withDatabaseName("my_iam")
        .withUsername("user")
        .withPassword("password")
        .withCopyFileToContainer(
            MountableFile.forHostPath(jooqSchemaDir.asFile.path),
            "/docker-entrypoint-initdb.d"
        )

    // jooq { } ブロックで組んだ Configuration と同一インスタンスを参照している
    val jdbc = jooq.configurations.getByName("main").jooqConfiguration.jdbc

    doFirst {
        container.start()
        jdbc.url = container.jdbcUrl
    }
    doLast {
        container.stop()
    }
}

// -----------------------------------------------------------------------------
// コンパイルオプション
// -----------------------------------------------------------------------------
tasks.withType<JavaCompile> {
    // リフレクション用にメソッド引数名を保持
    options.compilerArgs.add("-parameters")
    options.encoding = "UTF-8"
}
