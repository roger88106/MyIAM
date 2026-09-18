-- ============================================================================
-- MyIAM テストデータ（開発環境専用）
--
-- 内容：
--   - テストユーザー（test@test.com / パスワード password01）  … USER_MANAGER + VIEWER + LEGACY_ADMIN（一部権限なし）
--   - 管理者ユーザー（admin@test.com / パスワード password01） … ADMIN（全権限）
--   - テスト用 OAuth2 クライアント（public-client、シークレットは noop）
--   - 停用ロール LEGACY_ADMIN（ROLES.ENABLED フィルタの動作確認用）
--   - テストユーザーへのロール割当
--
-- ⚠ 本番環境では絶対に流さないこと。
-- ============================================================================

-- ============================== テストユーザー ==============================
-- sub = cc2b8ab4-3a3e-4583-9926-9f8cdef13dd9
insert into identity.users
    (id, username, email, password, enabled, password_changed_at, last_login_at, password_locked,
     created_at, created_by, version)
values
    ('cc2b8ab4-3a3e-4583-9926-9f8cdef13dd9',
     'test@test.com',
     'test@test.com',
     '{bcrypt}$2a$10$QKQ3k9nrLgKG1//Xg95G6OKTFork9kfKZw4O9pVvNqYVPC8xATXnO',
     true,
     '2026-08-20 12:24:48.466132+00',
     null,
     false,
     '2026-08-20 12:24:48.466132+00',
     'system',
     0)
on conflict (id) do nothing;

insert into identity.user_profiles (id, family_name, given_name)
values
    ('cc2b8ab4-3a3e-4583-9926-9f8cdef13dd9', 'テスト', '太郎')
on conflict (id) do nothing;

-- ============================== 管理者ユーザー ==============================
-- sub = 3c5d0ca0-405a-45d9-aeda-e661efb1d2df（パスワードはテストユーザーと同じ）
insert into identity.users
    (id, username, email, password, enabled, password_changed_at, last_login_at, password_locked,
     created_at, created_by, version)
values
    ('3c5d0ca0-405a-45d9-aeda-e661efb1d2df',
     'admin@test.com',
     'admin@test.com',
     '{bcrypt}$2a$10$QKQ3k9nrLgKG1//Xg95G6OKTFork9kfKZw4O9pVvNqYVPC8xATXnO',
     true,
     '2026-09-19 00:00:00+00',
     null,
     false,
     '2026-09-19 00:00:00+00',
     'system',
     0)
on conflict (id) do nothing;

insert into identity.user_profiles (id, family_name, given_name)
values
    ('3c5d0ca0-405a-45d9-aeda-e661efb1d2df', '管理', '太郎')
on conflict (id) do nothing;

-- ============================== テスト用クライアント ==============================
-- redirect 先は http://localhost:8081（別プロセスのテスト用 client アプリを想定）
insert into auth.oauth2_registered_client
    (id, client_id, client_id_issued_at, client_secret, client_secret_expires_at, client_name,
     client_authentication_methods, authorization_grant_types, redirect_uris, post_logout_redirect_uris, scopes,
     client_settings, token_settings, is_enabled, created_at, is_deleted, version)
values
    ('6a9b50ad-f9a7-31e1-2b99-6ab72f38cdb4',
     'public-client',
     '2026-05-01 00:00:00+00',
     '{noop}public-secret',
     null,
     'public-client',
     '{client_secret_post,none}',
     '{refresh_token,authorization_code}',
     '{http://localhost:8081/login/oauth2/code/public-client}',
     '{http://localhost:8081/}',
     '{openid,offline_access,profile,email,admin:access,user:read,user:write,user:delete,role:read,role:assign,permission:read,actuator:read}',
     '{"@class": "java.util.Collections$UnmodifiableMap", "settings.client.require-proof-key": true, "settings.client.require-authorization-consent": false}'::jsonb,
     '{"@class": "java.util.Collections$UnmodifiableMap", "settings.token.access-token-format": {"value": "self-contained", "@class": "org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat"}, "settings.token.reuse-refresh-tokens": false, "settings.token.access-token-time-to-live": ["java.time.Duration", "PT15M"], "settings.token.refresh-token-time-to-live": ["java.time.Duration", "PT1H"], "settings.token.id-token-signature-algorithm": ["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm", "RS256"], "settings.token.authorization-code-time-to-live": ["java.time.Duration", "PT5M"]}'::jsonb,
     true,
     '2026-05-25 11:32:45.391876+00',
     false,
     0)
on conflict (id) do nothing;

-- ============================== 停用ロール（フィルタ確認用） ==============================
-- user:delete を唯一持つロールを停用状態にしておく → 展開結果に user:delete が出なければフィルタ OK
insert into permission.roles (id, role, role_name, enabled, created_by)
values
    (gen_random_uuid(), 'LEGACY_ADMIN', '旧管理者（停用）', false, 'system')
on conflict (role) do nothing;

insert into permission.roles_permissions (role_id, permission_id)
select r.id, p.id
from permission.roles r, permission.permissions p
where r.role = 'LEGACY_ADMIN'
  and p.permission in ('admin:access', 'user:delete')
on conflict do nothing;

-- ============================== ロール割当 ==============================
-- テストユーザー: USER_MANAGER + VIEWER + LEGACY_ADMIN（停用）
-- 期待される permissions claim:
--   admin:access, user:read, user:write, role:read, permission:read
--   （user:delete は停用ロール経由のみ → 含まれない / role:assign は誰も持たない → 含まれない）
insert into permission.subjects_roles (subject, role_id)
select 'cc2b8ab4-3a3e-4583-9926-9f8cdef13dd9', r.id
from permission.roles r
where r.role in ('USER_MANAGER', 'VIEWER', 'LEGACY_ADMIN')
on conflict do nothing;

-- 管理者ユーザー: ADMIN（全権限）
insert into permission.subjects_roles (subject, role_id)
select '3c5d0ca0-405a-45d9-aeda-e661efb1d2df', r.id
from permission.roles r
where r.role = 'ADMIN'
on conflict do nothing;
