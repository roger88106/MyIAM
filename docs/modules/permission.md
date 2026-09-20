# permission モジュール

role / permission のカタログと、subject への role 割り当てを担当する。

依存：`identity::event`

## 認可モデル（permission-first RBAC）

- 認可の判定で見るのは **permission 文字列だけ**（`@PreAuthorize("hasAuthority('user:read')")`）。role は permission の束ね方に過ぎない
- scope の語彙 = permission の語彙。別の対応表は持たない。`openid` `profile` `email` はプロトコルの語彙なので permission には入らない
- subject は `sub` claim の文字列。ユーザー（user id）もクライアント（client id）も同じテーブルで扱う

## SubjectRoles アグリゲート

| 操作           | 内容                | イベント       |
|----------------|---------------------|----------------|
| `assign(role)` | role を付与（冪等） | `RoleAssigned` |
| `revoke(role)` | role を剥奪（冪等） | `RoleRevoked`  |

- 永続化は差分更新（追加分 insert / 削除分 delete）

## API（HTTP）

| Method | Path | 権限 |
|---|---|---|
| GET | `/api/subjects/{subject}/permissions` | `permission:read` |
| PUT | `/api/subjects/{subject}/roles/{role}` | `role:assign` |
| DELETE | `/api/subjects/{subject}/roles/{role}` | `role:assign` |

## 他モジュールへの公開

- `api/`（`@NamedInterface("api")`）：subject の有効権限を返す（`auth.permdir` が使う）
- `event/`（`@NamedInterface("event")`）：`RoleAssigned` / `RoleRevoked`

## 購読するイベント

- `identity.UserRegistered` → 既定の role `USER` を割り当てる
