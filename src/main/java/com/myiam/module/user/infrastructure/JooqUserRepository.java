package com.myiam.module.user.infrastructure;

import com.myiam.module.user.domain.user.User;
import com.myiam.module.user.domain.user.UserIdentity;
import com.myiam.module.user.domain.user.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JooqUserRepository implements UserRepository {
    @Override
    public boolean existsByIdentity(UserIdentity identity) {
        return false;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public void save(User user) {

    }
}
