-- ============================================================================
-- MyIAM スキーマ定義
--
-- docker compose の postgres コンテナ初回起動時（volume が空のとき）に自動実行される。
-- 既存 volume には適用されないため、変更時は手動で ALTER するか volume を作り直すこと。
--
-- スキーマ構成：
--   auth       : OAuth2 認可サーバー（クライアント / JWK / 同意）
--   user       : ユーザー
--   permission : 権限（ロール / 権限 / 割当）
--   common     : Spring Modulith イベント発行テーブル
-- ============================================================================

create schema if not exists auth;
create schema if not exists "user";
create schema if not exists permission;
create schema if not exists common;

-- ============================================================================
-- auth
-- ============================================================================

create table auth.oauth2_registered_client
(
    id                            uuid                                               not null
        constraint oauth2_registered_client_pkc
            primary key,
    client_id                     varchar(100)                                       not null,
    client_id_issued_at           timestamp with time zone                           not null,
    client_secret                 varchar(200)             default NULL::character varying,
    client_secret_expires_at      timestamp with time zone,
    client_name                   varchar(200)                                       not null,
    client_authentication_methods varchar(100)[]                                     not null,
    authorization_grant_types     varchar(100)[]                                     not null,
    redirect_uris                 varchar(1000)[]          default NULL::character varying[],
    post_logout_redirect_uris     varchar(1000)[]          default NULL::character varying[],
    scopes                        varchar(100)[]           default NULL::character varying[],
    client_settings               jsonb                                              not null,
    token_settings                jsonb                                              not null,
    is_enabled                    boolean                  default true              not null,
    created_at                    timestamp with time zone default CURRENT_TIMESTAMP not null,
    created_by                    varchar(255)             default NULL::character varying,
    updated_at                    timestamp with time zone,
    updated_by                    varchar(255)             default NULL::character varying,
    is_deleted                    boolean                  default false             not null,
    version                       integer                  default 0                 not null
);

comment on table auth.oauth2_registered_client is '登録クライアントマスタ';

comment on column auth.oauth2_registered_client.id is 'ID';

comment on column auth.oauth2_registered_client.client_id is 'クライアントID';

comment on column auth.oauth2_registered_client.client_id_issued_at is 'クライアントID発行日時:Instant型の日時';

comment on column auth.oauth2_registered_client.client_secret is 'クライアントシークレット';

comment on column auth.oauth2_registered_client.client_secret_expires_at is 'クライアントシークレット有効期限:Instant型の日時';

comment on column auth.oauth2_registered_client.client_name is 'クライアント名称';

comment on column auth.oauth2_registered_client.client_authentication_methods is 'クライアント認証方式';

comment on column auth.oauth2_registered_client.authorization_grant_types is '認可グラントタイプ';

comment on column auth.oauth2_registered_client.redirect_uris is 'リダイレクトURI';

comment on column auth.oauth2_registered_client.post_logout_redirect_uris is 'ログアウト後リダイレクトURI';

comment on column auth.oauth2_registered_client.scopes is '認可スコープリスト';

comment on column auth.oauth2_registered_client.client_settings is 'クライアント設定';

comment on column auth.oauth2_registered_client.token_settings is 'トークン設定';

comment on column auth.oauth2_registered_client.is_enabled is '有効フラグ: 0 : 無効, 1 : 有効';

comment on column auth.oauth2_registered_client.created_at is '作成日時:共通カラム';

comment on column auth.oauth2_registered_client.created_by is '作成者:共通カラム';

comment on column auth.oauth2_registered_client.updated_at is '更新日時:共通カラム';

comment on column auth.oauth2_registered_client.updated_by is '更新者:共通カラム';

comment on column auth.oauth2_registered_client.is_deleted is '削除フラグ:共通カラム';

comment on column auth.oauth2_registered_client.version is 'バージョン:共通カラム';

create table auth.oauth2_jwks
(
    id         uuid                                               not null
        constraint oauth2_jwks_pkc
            primary key,
    key        json                                               not null,
    is_enabled boolean                                            not null,
    created_at timestamp with time zone default CURRENT_TIMESTAMP not null,
    expires_at timestamp with time zone                           not null,
    created_by varchar(255)             default NULL::character varying,
    updated_at timestamp with time zone,
    updated_by varchar(255)             default NULL::character varying,
    is_deleted boolean                  default false             not null,
    version    integer                  default 0                 not null
);

comment on table auth.oauth2_jwks is 'OAuth2 JWKS情報';

comment on column auth.oauth2_jwks.id is 'JWTのkid';

comment on column auth.oauth2_jwks.key is 'JWK のキー情報';

comment on column auth.oauth2_jwks.is_enabled is '有効フラグ: 0 : 無効, 1 : 有効';

comment on column auth.oauth2_jwks.created_at is '作成日時';

comment on column auth.oauth2_jwks.expires_at is '有効期限: キーローテーション用';

comment on column auth.oauth2_jwks.created_by is '作成者:共通カラム';

comment on column auth.oauth2_jwks.updated_at is '更新日時:共通カラム';

comment on column auth.oauth2_jwks.updated_by is '更新者:共通カラム';

comment on column auth.oauth2_jwks.is_deleted is '削除フラグ:共通カラム';

comment on column auth.oauth2_jwks.version is 'バージョン:共通カラム';

create table auth.oauth2_authorization_consent
(
    registered_client_id uuid                                                       not null,
    principal_name       varchar(200)                                               not null,
    authorities          varchar(100)[]           default NULL::character varying[] not null,
    created_at           timestamp with time zone default CURRENT_TIMESTAMP         not null,
    created_by           varchar(255)             default NULL::character varying,
    updated_at           timestamp with time zone,
    updated_by           varchar(255)             default NULL::character varying,
    is_deleted           boolean                  default false                     not null,
    version              integer                  default 0                         not null,
    constraint oauth2_authorization_consent_pkc
        primary key (registered_client_id, principal_name)
);

comment on table auth.oauth2_authorization_consent is '認可同意情報';

comment on column auth.oauth2_authorization_consent.registered_client_id is '登録クライアントID';

comment on column auth.oauth2_authorization_consent.principal_name is '認可者名称:ユーザーID等';

comment on column auth.oauth2_authorization_consent.authorities is '権限リスト';

comment on column auth.oauth2_authorization_consent.created_at is '作成日時:共通カラム';

comment on column auth.oauth2_authorization_consent.created_by is '作成者:共通カラム';

comment on column auth.oauth2_authorization_consent.updated_at is '更新日時:共通カラム';

comment on column auth.oauth2_authorization_consent.updated_by is '更新者:共通カラム';

comment on column auth.oauth2_authorization_consent.is_deleted is '削除フラグ:共通カラム';

comment on column auth.oauth2_authorization_consent.version is 'バージョン:共通カラム';

-- ============================================================================
-- user
-- ============================================================================

create table "user".users
(
    id                  uuid                                               not null
        constraint user_pk
            primary key,
    username            varchar(128)
        constraint username_uk
            unique,
    email               varchar(255)                                       not null
        constraint email_uk
            unique,
    password            varchar(255)                                       not null,
    enabled             boolean                                            not null,
    password_changed_at timestamp with time zone                           not null,
    last_login_at       timestamp with time zone,
    password_locked     boolean                  default false             not null,
    created_at          timestamp with time zone default CURRENT_TIMESTAMP not null,
    created_by          varchar(255)                                       not null,
    updated_at          timestamp with time zone,
    updated_by          varchar(255)             default NULL::character varying,
    version             bigint                   default 0                 not null
);

comment on table "user".users is 'ユーザー';

comment on column "user".users.id is 'ユーザー ID';

comment on column "user".users.username is 'ユーザー名';

comment on column "user".users.email is 'ユーザーメールアドレス';

comment on column "user".users.password is 'パスワード';

comment on column "user".users.enabled is '有効フラグ';

comment on column "user".users.password_changed_at is 'パスワード変更日時';

comment on column "user".users.last_login_at is '最終ログイン日時';

comment on column "user".users.password_locked is 'パスワードロック中フラグ';

comment on column "user".users.created_at is '作成日時';

comment on column "user".users.created_by is '監査フィールド: 作成者';

comment on column "user".users.updated_at is '監査フィールド: 更新日時';

comment on column "user".users.updated_by is '監査フィールド: 更新者';

comment on column "user".users.version is 'バージョン';

create table "user".user_profiles
(
    id          uuid not null
        constraint user_profile_pk
            primary key
        constraint user_profile_userid_fk
            references "user".users,
    family_name varchar(50),
    given_name  varchar(50)
);

comment on table "user".user_profiles is 'ユーザープロファイル';

comment on column "user".user_profiles.id is 'ユーザー ID';

comment on column "user".user_profiles.family_name is '姓';

comment on column "user".user_profiles.given_name is '名前';

-- ============================================================================
-- common
-- ============================================================================

-- Spring Modulith のイベント発行テーブル（構造は Modulith 規定のもの）
create table common.event_publication
(
    id                     uuid not null
        constraint event_publication_pk
            primary key,
    listener_id            text,
    event_type             text,
    serialized_event       text,
    publication_date       timestamp with time zone,
    completion_date        timestamp with time zone,
    status                 text,
    completion_attempts    integer,
    last_resubmission_date timestamp with time zone
);

-- ============================================================================
-- permission
-- ============================================================================

create table permission.roles
(
    id         uuid                                               not null
        constraint roles_pk
            primary key,
    role       varchar(255)                                       not null
        constraint roles_uk_role
            unique,
    role_name  varchar(255),
    enabled    boolean                                            not null,
    created_at timestamp with time zone default CURRENT_TIMESTAMP not null,
    created_by varchar(255)                                       not null,
    updated_at timestamp with time zone,
    updated_by varchar(255),
    version    bigint                   default 0                 not null
);

comment on table permission.roles is 'ロール';

comment on column permission.roles.id is 'ロールID';

comment on column permission.roles.role is 'ロール';

comment on constraint roles_uk_role on permission.roles is 'ユニーク：ロール';

comment on column permission.roles.role_name is 'ロール名';

comment on column permission.roles.enabled is '有効フラグ';

comment on column permission.roles.created_at is '監査フィールド: 作成日時';

comment on column permission.roles.created_by is '監査フィールド: 作成者';

comment on column permission.roles.updated_at is '監査フィールド: 更新日時';

comment on column permission.roles.updated_by is '監査フィールド: 更新者';

comment on column permission.roles.version is 'バージョン';

create table permission.permissions
(
    id              uuid                                               not null
        constraint permissions_pk
            primary key,
    permission      varchar(255)                                       not null
        constraint permissions_uk_permission
            unique,
    permission_name varchar(255),
    created_at      timestamp with time zone default CURRENT_TIMESTAMP not null,
    created_by      varchar(255)                                       not null,
    updated_at      timestamp with time zone,
    updated_by      varchar(255),
    version         bigint                   default 0                 not null
);

comment on table permission.permissions is '権限';

comment on column permission.permissions.id is 'ID';

comment on column permission.permissions.permission is '権限';

comment on constraint permissions_uk_permission on permission.permissions is 'ユニーク：権限';

comment on column permission.permissions.permission_name is '権限名';

comment on column permission.permissions.created_at is '監査フィールド: 作成日時';

comment on column permission.permissions.created_by is '監査フィールド: 作成者';

comment on column permission.permissions.updated_at is '監査フィールド: 更新日時';

comment on column permission.permissions.updated_by is '監査フィールド: 更新者';

comment on column permission.permissions.version is 'バージョン';

create table permission.roles_permissions
(
    role_id       uuid not null
        constraint roles_permissions_role_id_fk
            references permission.roles,
    permission_id uuid not null
        constraint roles_permissions_permission_id_fk
            references permission.permissions,
    constraint roles_permissions_pk
        primary key (role_id, permission_id)
);

comment on table permission.roles_permissions is 'ロール権限表';

comment on column permission.roles_permissions.role_id is 'ロールID';

comment on constraint roles_permissions_role_id_fk on permission.roles_permissions is '外部キー：ロールID';

comment on column permission.roles_permissions.permission_id is '権限ID';

comment on constraint roles_permissions_permission_id_fk on permission.roles_permissions is '外部キー：権限ID';

-- subject は JWT の sub クレーム（ユーザーID / client_id）。
-- 他モジュールの主体を参照するため、意図的に FK を設定しない（孤児行はイベントで掃除する）。
create table permission.subjects_roles
(
    subject varchar(255) not null,
    role_id uuid         not null
        constraint subjects_roles_role_id_fk
            references permission.roles,
    constraint subjects_roles_pk
        primary key (subject, role_id)
);

comment on table permission.subjects_roles is 'サブジェクトロール表';

comment on column permission.subjects_roles.subject is 'サブジェクト（JWT の sub）';

comment on column permission.subjects_roles.role_id is 'ロールID';

comment on constraint subjects_roles_role_id_fk on permission.subjects_roles is '外部キー：ロールID';

