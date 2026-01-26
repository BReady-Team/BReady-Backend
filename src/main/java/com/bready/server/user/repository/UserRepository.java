package com.bready.server.user.repository;

import com.bready.server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query("""
        select u from User u
        join fetch u.userProfile
        where u.id = :userId
    """)
    Optional<User> findByIdWithProfile(Long userId);

}