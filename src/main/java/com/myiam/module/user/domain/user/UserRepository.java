package com.myiam.module.user.domain.user;

import org.jmolecules.ddd.annotation.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository {
    Optional<User> findById(UUID id);
    void save(User user);
    void remove(User user);
}
