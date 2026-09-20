-- ============================================================================
-- MyIAM 初期データ：権限カタログ
--
-- permission / role / role→permission の初期値（環境を問わず必要なもの）。
-- subject への割当やテスト用アカウントは 03_seed_test_data.sql を参照。
-- ============================================================================

-- ============================== permissions ==============================
insert into permission.permissions (id, permission, permission_name, created_by)
values
    (gen_random_uuid(), 'admin:access',    '管理コンソールへのアクセス', 'system'),
    (gen_random_uuid(), 'user:read',       'ユーザー閲覧',               'system'),
    (gen_random_uuid(), 'user:write',      'ユーザー編集',               'system'),
    (gen_random_uuid(), 'user:delete',     'ユーザー削除',               'system'),
    (gen_random_uuid(), 'role:read',       'ロール閲覧',                 'system'),
    (gen_random_uuid(), 'role:assign',     'ロール割当',                 'system'),
    (gen_random_uuid(), 'permission:read', '権限閲覧',                   'system'),
    (gen_random_uuid(), 'actuator:read',   '運用情報閲覧（actuator）',   'system')
on conflict (permission) do nothing;

-- ============================== roles ==============================
-- USER はユーザー登録時に自動割当されるデフォルトロール（権限なし）。
-- ロール名は PermissionCommandService.DEFAULT_ROLE と一致させること。
insert into permission.roles (id, role, role_name, enabled, created_by)
values
    (gen_random_uuid(), 'ADMIN',        '管理者',         true, 'system'),
    (gen_random_uuid(), 'USER_MANAGER', 'ユーザー管理者', true, 'system'),
    (gen_random_uuid(), 'VIEWER',       '閲覧者',         true, 'system'),
    (gen_random_uuid(), 'USER',         '一般ユーザー',   true, 'system')
on conflict (role) do nothing;

-- ============================== roles_permissions ==============================
-- ADMIN: 全権限
insert into permission.roles_permissions (role_id, permission_id)
select r.id, p.id
from permission.roles r, permission.permissions p
where r.role = 'ADMIN'
on conflict do nothing;

-- USER_MANAGER: 管理画面 + ユーザー読み書き + ロール閲覧 + 運用情報（削除・割当なし）
insert into permission.roles_permissions (role_id, permission_id)
select r.id, p.id
from permission.roles r, permission.permissions p
where r.role = 'USER_MANAGER'
  and p.permission in ('admin:access', 'user:read', 'user:write', 'role:read', 'actuator:read')
on conflict do nothing;

-- VIEWER: 管理画面 + 閲覧系のみ
insert into permission.roles_permissions (role_id, permission_id)
select r.id, p.id
from permission.roles r, permission.permissions p
where r.role = 'VIEWER'
  and p.permission in ('admin:access', 'user:read', 'role:read', 'permission:read')
on conflict do nothing;
