# テスト

## 構成

| source set | 内容 | 依存 |
|---|---|---|
| `src/test` | 単体：アグリゲート・値オブジェクトの振る舞い。JUnit + AssertJ のみ、Spring を起動しない | なし |
| `src/integrationTest` | 統合：`@SpringBootTest` + Testcontainers（PostgreSQL / Redis） | Docker |

```bash
# 単体
./gradlew test
# 統合
./gradlew integrationTest
# 両方
./gradlew build
```

どちらも `main` と同じパッケージ構成で置く（package-private を直接テストするため）。

## 統合テストの種類

| 種類 | 保証すること |
|---|---|
| Modulith（`ModulithTest`） | モジュール間の依存が `allowedDependencies` の宣言通りであること |
| 契約：公開エンドポイント | 公開と宣言したもの以外の全マッピングが、匿名では 401 / 403 になること |
| 契約：権限カタログ | コードが要求する権限（`hasAuthority('...')`）が DB のカタログに必ず存在すること |
| Repository | アグリゲートの永続化・再構築・差分更新・イベント発行 |
| OAuth2 フロー | authorize → login → token → refresh、RT 再利用検知、ロックアウト（MockMvc、ブラウザ無し） |

契約テストは「増やしたら許可リストに足す」形にしてあり、追加漏れがレビューで見えるようにしている。

## 環境

- Testcontainers は `docker/postgres/init/*.sql` をそのまま流す。compose・jOOQ コード生成と同じスキーマ
- コンテナは JVM 内で共有。テスト間で状態を持ち越さないよう、書き込む場合は `@Transactional`（ロールバック）かテストごとに一意な subject を使う
- E2E は自動化しない（`.local/http/*.http` で手動）
