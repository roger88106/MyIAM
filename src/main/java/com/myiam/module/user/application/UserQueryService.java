package com.myiam.module.user.application;

import com.myiam.common.error.exception.BusinessException;
import com.myiam.module.user.application.model.query.UserCredentialQuery;
import com.myiam.module.user.application.model.view.UserClaimsView;
import com.myiam.module.user.application.model.view.UserCredentialView;
import com.myiam.module.user.application.model.view.UserDetailView;
import com.myiam.module.user.domain.user.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

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
     * ユーザー取得
     *
     * @param userId ユーザーID
     * @return ユーザー情報
     */
    public UserDetailView getUserById(UUID userId) {
        return userQueryRepository.findUserById(userId)
                .orElseThrow(() -> BusinessException.of(UserErrorCode.USER_NOT_FOUND));
    }

    // ============================== 認証認処理用 ==============================

    /**
     * ユーザー認証情報取得
     *
     * @param query ユーザー認証情報クエリ
     * @return ユーザー認証情報
     */
    public Optional<UserCredentialView> findUserCredential(UserCredentialQuery query) {
        return userQueryRepository.findUserCredential(query);
    }

    /**
     * ユーザークレーム取得
     *
     * @param userId ユーザーID
     * @return ユーザークレーム
     */
    public Optional<UserClaimsView> findUserClaims(UUID userId) {
        return userQueryRepository.findUserClaims(userId);
    }

}
