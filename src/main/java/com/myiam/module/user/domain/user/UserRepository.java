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
     * ユーザー存在チェック
     *
     * @param identity ユーザーの識別情報
     * @return 存在する場合 {@code true}, 存在しない場合 {@code false}
     */
    boolean existsByIdentity(UserIdentity identity);

    /**
     * ユーザー取得
     *
     * @param id ユーザー ID
     * @return ユーザー ドメインモデル
     */
    Optional<User> findById(UUID id);

    /**
     * ユーザー登録
     *
     * @param user ユーザー ドメインモデル
     */
    void add(User user);

    /**
     * ユーザー保存
     *
     * @param user ユーザー ドメインモデル
     */
    void save(User user);
}
