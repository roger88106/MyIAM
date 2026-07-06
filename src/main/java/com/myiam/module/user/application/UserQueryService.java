package com.myiam.module.user.application;

import com.myiam.module.user.application.model.query.UserClaimsQuery;
import com.myiam.module.user.application.model.query.UserCredentialQuery;
import com.myiam.module.user.application.model.result.UserClaims;
import com.myiam.module.user.application.model.result.UserCredential;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * ユーザークエリサービス
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryService {

    /**
     * ユーザー検索リポジトリ
     */
    private final UserQueryRepository userQueryRepository;

    /**
     * ユーザー認証情報取得
     *
     * @param query ユーザー認証情報クエリ
     * @return ユーザー認証情報
     */
    public Optional<UserCredential> findUserCredential(UserCredentialQuery query) {
        return userQueryRepository.findUserCredential(query);
    }

    /**
     * ユーザークレーム取得
     *
     * @param query ユーザークレームクエリ
     * @return ユーザークレーム
     */
    public Optional<UserClaims> findUserClaims(UserClaimsQuery query) {
        return userQueryRepository.findUserClaims(query);
    }


}
