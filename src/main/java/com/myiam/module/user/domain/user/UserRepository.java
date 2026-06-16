package com.myiam.module.user.domain.user;

import org.jmolecules.ddd.annotation.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * ユーザーリポジトリ
 */
@Repository
public interface UserRepository {

    /**
     * ユーザー取得
     *
     * @param id ユーザー ID
     * @return ユーザー ドメインモデル
     */
    Optional<User> findById(UUID id);

    /**
     * ユーザー保存
     *
     * @param user ユーザー ドメインモデル
     */
    void save(User user);

    /**
     * ユーザー削除
     *
     * @param user ユーザー ドメインモデル
     */
    void remove(User user);
}
