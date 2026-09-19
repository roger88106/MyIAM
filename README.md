# MyIAM

Spring Boot 4 / Java 21 で実装した OAuth 2.1 / OIDC 認可サーバー。
Spring Modulith によるモジュラーモノリスの上に、DDD + Clean Architecture で組んでいる。

> 学習・ポートフォリオ目的のプロジェクト。設計上の判断とその理由は [docs/architecture.md](docs/architecture.md) を参照。

## できること

- 認証（Spring Security）
- 認可（Spring Authorization Server による OAuth 2.1 / OIDC）
- ユーザー情報管理
- 権限管理 （RBAC モデル）

## 技術スタック

| 領域           | 採用                                                                   |
|----------------|------------------------------------------------------------------------|
| Framework      | Spring Boot 4, Spring Authorization Server, Spring Security            |
| モジュール構成 | Spring Modulith, jMolecules                                            |
| 永続化         | PostgreSQL + jOOQ（コード生成）、Redis（認可コード / token / session） |
| ビルド         | Gradle (Kotlin DSL), version catalog                                   |
| テスト         | JUnit 5, AssertJ, Testcontainers, MockMvc                              |

## 動かす

前提：JDK 21、Docker

```bash
# PostgreSQL / Redis を起動（初回起動時のみ docker/postgres/init/*.sql が実行される）
docker compose up -d

# 起動（local profile：接続先・開発用ログレベル等）
./gradlew bootRun --args='--spring.profiles.active=local'
```

- issuer：`http://localhost:8080`
- OIDC discovery：`http://localhost:8080/.well-known/openid-configuration`
- テスト用アカウント / クライアントは `docker/postgres/init/03_seed_test_data.sql`

## テスト

```bash
# 単体テスト
./gradlew test
# 統合テスト（Spring + Testcontainers、Docker 必須）
./gradlew integrationTest
# ビルド
./gradlew build
```

統合テストには、モジュール境界の検証（Modulith）、公開エンドポイントの契約テスト、
権限カタログの契約テスト、OAuth2 フロー（authorize → token → refresh → 再利用検知）のテストが含まれる。

## 構成

```
com.myiam
├── module/
│   ├── auth/         AuthN / AuthZ に関連する実装 ※Spring Authorization Server など
│   ├── identity/     ユーザーなどの識別情報の管理
│   └── permission/   ロール・権限の割り当ての管理
├── common/           例外階層、i18n などの共通処理
└── config/           Security filter chain、Validation
```

詳細は [docs/architecture.md](docs/architecture.md)。

## テーブル定義

DDL と初期データは [docker/postgres/init/](docker/postgres/init) にある。

## 制約・既知の割り切り

- フロントエンドは対象外（純粋なバックエンド AS）
- 監査ログ、role の CRUD、メール認証付きの本登録は未実装
