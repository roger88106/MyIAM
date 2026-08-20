package com.myiam.module.auth.jwk;

import com.myiam.common.error.exception.SystemException;
import com.myiam.module.auth.shared.constant.CacheNameConst;
import com.nimbusds.jose.jwk.JWK;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.jooq.impl.DSL;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.text.ParseException;
import java.util.List;
import java.util.UUID;

import static com.myiam.jooq.auth.tables.Oauth2Jwks.OAUTH2_JWKS;

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
    @Cacheable(value = CacheNameConst.JWK, key = "#root.methodName")
    public List<JWK> selectKeys() {
        return dsl.select(OAUTH2_JWKS.KEY)
                .from(OAUTH2_JWKS)
                // 有効
                .where(OAUTH2_JWKS.IS_ENABLED.isTrue())
                // 削除されていない
                .and(OAUTH2_JWKS.IS_DELETED.isFalse())
                // 有効期限が過ぎていない
                .and(OAUTH2_JWKS.EXPIRES_AT.gt(DSL.currentInstant()))
                // 作成時間で降順ソート
                .orderBy(OAUTH2_JWKS.CREATED_AT.desc())
                .fetch(record -> {
                    try {
                        return JWK.parse(record.get(OAUTH2_JWKS.KEY).toString());
                    } catch (ParseException e) {
                        throw SystemException.of("Jwk conversion failed.", e);
                    }
                });
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
    @CacheEvict(value = CacheNameConst.JWK, allEntries = true)
    public void storeKey(@NonNull JWK jwk) {
        var record = dsl.newRecord(OAUTH2_JWKS);

        // key情報をJsonに変換
        JSON key = JSON.valueOf(jwk.toJSONString());

        // 項目設定
        record.setId(UUID.fromString(jwk.getKeyID()));
        record.setKey(key);
        record.setIsEnabled(true);
        record.setCreatedAt(jwk.getIssueTime().toInstant());
        record.setExpiresAt(jwk.getExpirationTime().toInstant());

        // 共通カラム設定
        record.setCreatedBy("system");

        // データベースへ保存 (Insert)
        record.store();
    }
}
