package com.myiam.module.auth.server.token;

import com.google.common.hash.Hashing;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.core.OAuth2DeviceCode;
import org.springframework.security.oauth2.core.OAuth2UserCode;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis の認可情報リポジトリ。
 */
@Repository
@RequiredArgsConstructor
class RedisOAuth2AuthorizationRepository {

    /** Redis 文字列テンプレート */
    private final StringRedisTemplate redisTemplate;
    /** 認証情報コンバーター */
    private final AuthorizationConverter converter;

    /**
     * 認可 ID キー
     * <ul><li>パラメータ：認可ID</li></ul>
     */
    private static final String REDIS_KEY_ID = "oauth2:authorization:id:%s";

    /**
     * トークンマップ キー<br />
     * findByToken する際、Token で 認可ID を取得するためのマップ
     * <ul>
     *     <li>パラメータ：トークンタイプ</li>
     *     <li>パラメータ：トークンハッシュ</li>
     * </ul>
     */
    private static final String REDIS_KEY_TOKEN_MAP = "oauth2:authorization:token:%s:%s";

    /**
     * サポート対象のトークンタイプ
     */
    private static final Set<String> SUPPORTED_TOKEN_TYPES = Set.of(
            OAuth2ParameterNames.STATE,
            OAuth2ParameterNames.CODE,
            OAuth2TokenType.ACCESS_TOKEN.getValue(),
            OAuth2TokenType.REFRESH_TOKEN.getValue(),
            OidcParameterNames.ID_TOKEN,
            OAuth2ParameterNames.DEVICE_CODE,
            OAuth2ParameterNames.USER_CODE
    );

    /**
     * 認可情報を保存または更新する。
     *
     * @param authorization 保存対象の認可情報
     */

    void save(@NonNull OAuth2Authorization authorization) {

        // 秒単位のTTLを取得する
        long ttlBySecond = getMaxTTL(authorization);

        // 既存の認可情報が存在する場合、既存情報を削除 ※既存の逆引きインデックス含み
        OAuth2Authorization existing = findById(authorization.getId());
        if (existing != null) {
            remove(existing);
        }

        // 新規の認証認可情報をRedisに保存する
        redisTemplate.opsForValue().set(
                getIdKey(authorization.getId()),
                converter.serOAuth2Authorization(authorization),
                ttlBySecond,
                TimeUnit.SECONDS
        );

        // 各種トークンキーの逆引きインデックスを保存
        String authId = authorization.getId();
        extractTokenSnapshots(authorization).forEach(tokenSnapshot ->
                getTokenMapKeys(tokenSnapshot.value, tokenSnapshot.type).stream()
                        .findFirst()
                        .ifPresent(key -> redisTemplate.opsForValue().set(
                                key,
                                authId,
                                ttlBySecond,
                                TimeUnit.SECONDS)
                        )
        );
    }

    /**
     * 認可情報を物理削除する。
     *
     * @param authorization 削除対象の認可情報
     */
    void remove(@NonNull OAuth2Authorization authorization) {
        Assert.notNull(authorization, "authorization cannot be null");

        // 認可情報を削除
        redisTemplate.delete(getIdKey(authorization.getId()));

        // 各種トークンマップキーの逆引きインデックスを物理削除
        List<String> keysToDelete = extractTokenSnapshots(authorization).stream()
                .map(tokenSnapshot -> getTokenMapKeys(tokenSnapshot.value, tokenSnapshot.type))
                .flatMap(Collection::stream)
                .toList();

        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
        }
    }

    /**
     * ID キーから認可情報を検索する。
     *
     * @param id 認可ID
     * @return 該当する認可情報
     */
    OAuth2Authorization findById(@NonNull String id) {
        String json = redisTemplate.opsForValue().get(getIdKey(id));
        return converter.toAuthorization(json);
    }

    /**
     * トークン値およびトークン種別から認可情報を検索する。
     *
     * @param token トークン文字列
     * @param tokenType トークン種別（省略可能）
     * @return 該当する認可情報
     */
    OAuth2Authorization findByToken(@NonNull String token, @Nullable OAuth2TokenType tokenType) {
        // トークンマップキーを取得する
        String typeVal = tokenType != null ? tokenType.getValue() : null;
        Set<String> tokenKeys = getTokenMapKeys(token, typeVal);

        // トークンキーから認可IDを取得する
        Optional<String> idOptional = tokenKeys.stream()
                .map(redisTemplate.opsForValue()::get)
                .filter(Objects::nonNull)
                .findFirst();

        // IDがある場合、IDで認可情報を取得する
        return idOptional.map(this::findById).orElse(null);
    }

    /**
     * 認可ID キーを取得する
     *
     * @param authorizationId 認可ID
     * @return 認可ID キー
     */
    private String getIdKey(String authorizationId) {
        return REDIS_KEY_ID.formatted(authorizationId);
    }

    /**
     * トークンマップキーを取得する
     *
     * @param token トークン文字列
     * @param tokenType トークン種類 ※nullの場合は全件対象
     * @return トークンマップキーのセット
     */
    private Set<String> getTokenMapKeys(String token, String tokenType) {
        // tokenをハッシュ化
        String tokenHash = Hashing.sha256()
                .hashString(token, StandardCharsets.UTF_8)
                .toString();

        // TokenTypeが指定しない場合、サポート対象の全種類を返却する
        if (tokenType == null) {
            return SUPPORTED_TOKEN_TYPES.stream()
                    .map(type -> REDIS_KEY_TOKEN_MAP.formatted(type, tokenHash))
                    .collect(Collectors.toUnmodifiableSet());
        }

        // TokenType指定 かつ サポート対象の場合、対象のキーを返却する
        if (SUPPORTED_TOKEN_TYPES.contains(tokenType)) {
            return Set.of(REDIS_KEY_TOKEN_MAP.formatted(tokenType, tokenHash));
        }

        // TokenType指定 かつ サポート対象外の場合、空のSetを返却する
        return Set.of();
    }

    /**
     * 最大有効期限を計算する ※デフォルトは五分間を設定
     *
     * @param authorization OAuth2Authorizationクラス
     * @return 有効期限 ※秒単位
     */
    private long getMaxTTL(OAuth2Authorization authorization) {
        Instant now = Instant.now();
        Instant defaultMax = now.plus(5, ChronoUnit.MINUTES);

        // 最大の有効期限を取得
        Instant maxExpiresAt = extractTokenSnapshots(authorization).stream()
                .map(TokenSnapshot::expiresAt)
                .max(Instant::compareTo)
                .orElse(defaultMax);

        // デフォルトの5分より短い場合は5分を担保する
        if (maxExpiresAt.isBefore(defaultMax)) {
            maxExpiresAt = defaultMax;
        }

        return maxExpiresAt.getEpochSecond() - now.getEpochSecond();
    }

    /**
     * 認可情報から全アクティブトークンのスナップショットを抽出する。
     *
     * @param authorization 認可情報
     * @return トークンスナップショットのリスト
     */
    private List<TokenSnapshot> extractTokenSnapshots(OAuth2Authorization authorization) {
        List<TokenSnapshot> snapshots = new ArrayList<>();

        // State
        String stateVal = authorization.getAttribute(OAuth2ParameterNames.STATE);
        if (StringUtils.hasText(stateVal)) {
            snapshots.add(new TokenSnapshot(OAuth2ParameterNames.STATE, stateVal, null));
        }

        // 各トークンを統一的に抽出
        addTokenSnapshot(snapshots, OAuth2ParameterNames.CODE,
                authorization.getToken(OAuth2AuthorizationCode.class));
        addTokenSnapshot(snapshots, OAuth2TokenType.ACCESS_TOKEN.getValue(),
                authorization.getAccessToken());
        addTokenSnapshot(snapshots, OAuth2TokenType.REFRESH_TOKEN.getValue(),
                authorization.getRefreshToken());
        addTokenSnapshot(snapshots, OidcParameterNames.ID_TOKEN,
                authorization.getToken(OidcIdToken.class));
        addTokenSnapshot(snapshots, OAuth2ParameterNames.USER_CODE,
                authorization.getToken(OAuth2UserCode.class));
        addTokenSnapshot(snapshots, OAuth2ParameterNames.DEVICE_CODE,
                authorization.getToken(OAuth2DeviceCode.class));

        return snapshots;
    }

    private void addTokenSnapshot(List<TokenSnapshot> list, String type,
                                  OAuth2Authorization.Token<?> token) {
        if (token != null) {
            Instant expiresAt = token.getToken().getExpiresAt();
            list.add(new TokenSnapshot(type, token.getToken().getTokenValue(), expiresAt));
        }
    }

    /**
     * トークン情報のスナップショット。
     *
     * @param type トークン種別キー
     * @param value トークン値
     * @param expiresAt 有効期限
     */
    private record TokenSnapshot(String type, String value, Instant expiresAt) {}

}
