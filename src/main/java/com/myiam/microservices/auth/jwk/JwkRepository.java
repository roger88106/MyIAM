package com.myiam.microservices.auth.jwk;

import com.myiam.microservices.auth.core.constant.CacheName;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.myiam.jooq.myiam.tables.Oauth2Jwks.OAUTH2_JWKS;

/**
 * JWK リポジトリ
 */
@Repository
@RequiredArgsConstructor
class JwkRepository {

    /** DSL コンテキスト */
    private final DSLContext dsl;

    /**
     * 有効なJWK一覧の取得。 <strong>※検索結果はキャッシュされる</strong>
     *
     * @return 有効な JWK のリスト (作成時間降順でソート済み)
     */
    @Cacheable(value = CacheName.JWK, key = "#root.methodName")
    public List<Jwk> selectKeys() {
        return dsl.selectFrom(OAUTH2_JWKS)
                // 有効
                .where(OAUTH2_JWKS.IS_ENABLED.isTrue())
                // 削除されていない
                .and(OAUTH2_JWKS.IS_DELETED.isFalse())
                // 有効期限が過ぎていない
                .and(OAUTH2_JWKS.EXPIRES_AT.gt(DSL.currentInstant()))
                // 作成時間で降順ソート
                .orderBy(OAUTH2_JWKS.CREATED_AT.desc())
                .fetch(Jwk::from);
    }

    /**
     * 新規JWKの保存。<br />
     * 同時にキャッシュされたJWKを破棄する。
     *
     * <p>
     * <strong>注意: </strong>このメソッドは新規作成 (Insert) のみをサポートしています。
     * JWK は変更不可のため、更新 (Update) はサポートしていません。
     * </p>
     *
     * @param jwk 登録対象の JWK ドメイン
     */
    @CacheEvict(value = CacheName.JWK, allEntries = true)
    public void storeKey(@NonNull Jwk jwk) {
        var record = dsl.newRecord(OAUTH2_JWKS);

        // 項目設定
        record.setId(jwk.id());
        record.setKid(jwk.data().kid());
        record.setJwkJson(jwk.data().jwkJson());
        record.setIsEnabled(jwk.data().isEnabled());
        record.setExpiresAt(jwk.data().expiresAt());
        record.setCreatedAt(jwk.data().createdAt());

        // 共通カラム設定
        record.setCreatedBy("system");

        // データベースへ保存 (Insert)
        record.store();
    }
}
