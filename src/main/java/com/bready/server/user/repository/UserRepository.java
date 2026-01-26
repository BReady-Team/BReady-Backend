package com.bready.server.user.repository;

import com.bready.server.user.domain.User;
import com.bready.server.user.domain.UserAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByAuthProviderAndProviderUserId(
            UserAuthProvider authProvider,
            String providerUserId
    );

    @Query("""
        select u from User u
        join fetch u.userProfile
        where u.id = :userId
    """)
    Optional<User> findByIdWithProfile(Long userId);

}