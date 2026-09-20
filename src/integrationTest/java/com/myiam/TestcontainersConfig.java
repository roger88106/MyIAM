package com.myiam;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * 統合テスト用の Testcontainers 設定。<br />
 * PostgreSQL / Redis を使い捨てコンテナで起動し、{@code @ServiceConnection} で Spring Boot の接続設定を自動的に差し替える。
 * コンテナはテスト JVM 内で一度だけ起動され、全テストクラスで共有される。
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    /**
     * PostgreSQL コンテナ。<br />
     * docker/postgres/init 配下の SQL を初回起動時に流す（docker compose と同じ手順）。
     *
     * @return PostgreSQL コンテナ
     */
    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgres() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:18-alpine"))
                .withDatabaseName("my_iam")
                .withUsername("user")
                .withPassword("password")
                .withCopyFileToContainer(
                        MountableFile.forHostPath("docker/postgres/init"),
                        "/docker-entrypoint-initdb.d");
    }

    /**
     * Redis コンテナ。
     *
     * @return Redis コンテナ
     */
    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redis() {
        return new GenericContainer<>(DockerImageName.parse("redis:8-alpine"))
                .withExposedPorts(6379);
    }
}
