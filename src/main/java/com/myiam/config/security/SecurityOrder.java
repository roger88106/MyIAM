package com.myiam.config.security;

import lombok.NoArgsConstructor;

/**
 * SpringSecurity のフィルターチェンの順位
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class SecurityOrder {

    /**
     * 認証 フィルターチェン
     */
    public static final int AUTHENTICATION = 2;

    /**
     * 認可 フィルターチェン
     */
    public static final int AUTHORIZATION = 1;

    /**
     * リソース フィルターチェン
     */
    public static final int RESOURCE = 3;

    /**
     * デフォルト フィルターチェン
     */
    public static final int DEFAULT = 99;

}
