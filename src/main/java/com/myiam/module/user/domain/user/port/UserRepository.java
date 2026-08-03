package com.myiam.module.user.domain.user.port;

import com.myiam.module.user.domain.user.User;
import com.myiam.module.user.domain.user.vo.Email;
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
     * @param email ユーザーのメールアドレス
     * @return 存在する場合 {@code true}, 存在しない場合 {@code false}
     */
    boolean existsByIdentifier(Email email);

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
