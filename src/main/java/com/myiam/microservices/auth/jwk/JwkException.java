package com.myiam.microservices.auth.jwk;

import com.myiam.microservices.common.error.SystemException;

/**
 * JWK 関連のシステム例外。<br />
 * ※エラー追跡しやすくするためのラップ、単独のハンドラー実装しない想定
 */
class JwkException extends SystemException {
    JwkException(String message, Throwable cause) {
        super(message, cause);
    }
}
