import org.yaml.snakeyaml.Yaml

/*
 * MyIAM 認証サービス - ビルド構成定義 (Gradle Kotlin DSL)
 * 
 * Spring Boot の基盤設定、依存関係管理、および jooq によるデータベースコード自動生成パイプラインを定義します。
 */

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        // application.yml を解析するためのライブラリ
        classpath("org.yaml:snakeyaml:2.3")
    }
}

plugins {
    java
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("nu.studer.jooq") version "10.2.1"
}

extra["jooq.version"] = "3.21.4"

group = "com.myiam"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        // Java バージョン設定
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

configurations {
    compileOnly {
        // アノテーションプロセッサーをコンパイル時にも参照可能にする
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:2.1.0")
    }
}

dependencies {
    // --- Spring Boot ---
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    implementation("org.springframework.modulith:spring-modulith-events-api")
    implementation("org.springframework.modulith:spring-modulith-events-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // --- データベース & jooq 拡張 ---
    runtimeOnly("org.postgresql:postgresql")
    jooqGenerator("org.postgresql:postgresql:42.7.7")
    implementation("org.jooq:jooq-jackson3-extensions:3.21.4")

    // --- ユーティリティ (Lombok, MapStruct) ---
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    compileOnly("org.projectlombok:lombok:1.18.44")
    annotationProcessor("org.projectlombok:lombok:1.18.44")

    // Lombok と MapStruct の連携用バインディング
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    // AOP
    implementation("org.aspectj:aspectjweaver")

    // キャッシュ
    implementation("com.github.ben-manes.caffeine:caffeine")

    // ハッシュアルゴリズム
    implementation("com.google.guava:guava:33.6.0-android")

    // jMolecules
    implementation("org.jmolecules:jmolecules-ddd:1.9.0")

    // --- テストフレームワーク ---
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// -----------------------------------------------------------------------------
// データベース接続設定の動的読込 (Single Source of Truth: application.yml)
// -----------------------------------------------------------------------------
val dbConfig = mutableMapOf<String, String>()
val ymlFile = file("src/main/resources/application.yml")

if (ymlFile.exists()) {
    val yaml = Yaml()
    val config = ymlFile.inputStream().use { yaml.load<Map<String, Any>>(it) }
    
    // YAML 構造から datasource 情報を抽出
    val spring = config["spring"] as? Map<*, *>
    val datasource = spring?.get("datasource") as? Map<*, *>
    
    dbConfig["url"] = datasource?.get("url")?.toString() ?: ""
    dbConfig["user"] = datasource?.get("username")?.toString() ?: ""
    dbConfig["password"] = datasource?.get("password")?.toString() ?: ""
    dbConfig["driver"] = datasource?.get("driver-class-name")?.toString() ?: "org.postgresql.Driver"
}

// -----------------------------------------------------------------------------
// jooq コード生成タスク設定
// -----------------------------------------------------------------------------
jooq {
    // Spring Boot BOM から jooq の推奨バージョンを自動取得
    val jooqVersion = "3.21.4"
    version.set(jooqVersion)

    configurations {
        create("main") {
            jooqConfiguration.apply {
                // ログ出力レベルの設定
                logging = org.jooq.meta.jaxb.Logging.INFO

                // データベース接続情報
                jdbc.apply {
                    driver = dbConfig["driver"]
                    url = dbConfig["url"]
                    user = dbConfig["user"]
                    password = dbConfig["password"]
                }

                // ジェネレーター設定
                generator.apply {
                    name = "org.jooq.codegen.JavaGenerator"

                    // スキャン対象のデータベース設定
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        
                        // 全てのユーザスキーマを対象とする
                        includes = ".*"
                        // システム関連は除外する
                        excludes = "(information_schema|pg_catalog|pg_toast|pg_temp_.*|pg_toast_temp_.*)\\..*|" +
                                   "pgp_.*|armor|dearmor|crypt|digest|hmac|gen_salt|gen_random_bytes|gen_random_uuid"
                        
                        // バージョン管理用カラムの指定
                        recordVersionFields = "version"

                        // timestamptz を Instant に自動変換する設定
                        forcedTypes.addAll(listOf(
                            org.jooq.meta.jaxb.ForcedType().apply {
                                name = "INSTANT"
                                includeTypes = "(?i)timestamptz|timestamp\\ with\\ time\\ zone"
                            },
                            org.jooq.meta.jaxb.ForcedType().apply {
                                userType = "tools.jackson.databind.JsonNode"
                                isJsonConverter = true
                                includeTypes = "jsonb"
                            }
                        ))
                    }

                    // 生成コードのオプション
                    generate.apply {
                        // Setter メソッドのフルーエント API を有効化
                        isFluentSetters = true
                        // Java 8 の Date/Time API を使用
                        isJavaTimeTypes = true
                    }

                    // 生成先のパッケージとパス
                    target.apply {
                        // 出力先パッケージ定義
                        packageName = "com.myiam.jooq"
                        // 出力先はデフォルトを使用
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// コンパイルオプション設定
// -----------------------------------------------------------------------------
tasks.withType<JavaCompile> {
    // リフレクション用にメソッド引数名を保持
    options.compilerArgs.add("-parameters")
    // コンパイル時の文字エンコーディングを UTF-8 に固定
    options.encoding = "UTF-8"
}

tasks.named<Test>("test") {
    // JUnit 5 使用設定
    useJUnitPlatform()
}
