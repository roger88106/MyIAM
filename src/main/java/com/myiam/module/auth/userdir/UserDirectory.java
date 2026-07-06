package com.myiam.module.auth.userdir;

import java.util.Optional;
import java.util.UUID;

/**
 * ユーザーディレクトリ。<br />
 * ユーザーの情報を管理するためのインターフェース。
 */
public interface UserDirectory {

    /**
     * 認証情報を取得する
     *
     * @param loginId ログイン識別子
     * @return ユーザー認証情報
     */
    Optional<UserCredential> findCredential(String loginId);

    /**
     * ユーザークレームを取得する
     *
     * @param userId ユーザー ID
     * @return ユーザークレーム
     */
    Optional<UserClaims> findClaims(UUID userId);
}
