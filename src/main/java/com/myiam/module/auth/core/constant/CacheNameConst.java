package com.myiam.module.auth.core.constant;

import java.util.Set;

/**
 * キャッシュ名の定数クラス。
 */
public final class CacheNameConst {
    /**
     * JWK キャッシュ名 <br />
     * <br />
     * <strong>想定の格納内容：</strong>
     * <ul>
     *   <li>キー：メソッド名</li>
     *   <li>値：JWKドメインモデルのリスト</li>
     * </ul>
     */
    public static final String JWK = "jwk";

    /**
     * 登録クライアントキャッシュ名 <br />
     * <br />
     * <strong>想定の格納内容：</strong>
     * <ul>
     *   <li>キー：テーブルID</li>
     *   <li>値：登録クライアント(RegisteredClient.class)</li>
     * </ul>
     */
    public static final String REGISTERED_CLIENT = "registered_client";

    /**
     * 登録クライアントIDマップキャッシュ名<br />
     * <br />
     * <strong>想定の格納内容：</strong>
     * <ul>
     *   <li>キー：クライアントID</li>
     *   <li>値：テーブルID</li>
     * </ul>
     */
    public static final String REGISTERED_CLIENT_ID_MAP = "registered_client_id_map";

    /**
     * キャッシュ名のセット
     */
    public static final Set<String> CACHE_NAMES = Set.of(
            JWK, REGISTERED_CLIENT, REGISTERED_CLIENT_ID_MAP
    );

    /** コンストラクタをプライベート化 */
    private CacheNameConst() {
    }
}
