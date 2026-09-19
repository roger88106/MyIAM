# identity モジュール

ユーザーの識別情報とライフサイクルを担当する。

依存：なし

## User アグリゲート

| 操作                              | 内容                           | イベント                      |
|-----------------------------------|--------------------------------|-------------------------------|
| `register`                        | ユーザー登録                   | `UserRegistered`              |
| `changePassword`                  | 旧パスワードを検証した上で変更 | `PasswordChanged`             |
| `updateProfile`                   | ユーザープロフィールの更新     | `ProfileUpdated`              |
| `disable`                         | ユーザー無効化                 | `UserDisabled`                |
| `recordLogin`                     | 最終ログイン日時を更新         | `UserLoggedIn`                |
| `lockPassword` / `unlockPassword` | パスワードロック（冪等）       | `UserLocked` / `UserUnlocked` |

- 永続化は `snapshot()` → `UserFactory` で再構築

## API（HTTP）

| Method | Path                           | 権限          |
|--------|--------------------------------|---------------|
| POST   | `/api/users/register`          | 公開          |
| GET    | `/api/users/{userId}`          | `user:read`   |
| PUT    | `/api/users/{userId}/password` | `user:write`  |
| PUT    | `/api/users/{userId}/profile`  | `user:write`  |
| PUT    | `/api/users/{userId}/disable`  | `user:delete` |

## 他モジュールへの公開

- `api/auth`（`@NamedInterface("api")`）：`auth` 専用。認証用のユーザー取得、`recordLogin`、`lockPassword`
- `event/`（`@NamedInterface("event")`）：上の表のイベント
