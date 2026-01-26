package com.bready.server.user.domain;

import com.bready.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "users",
uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_users_provider",
                columnNames = {"auth_provider", "provider_user_id"}
        )
})
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    private UserAuthProvider authProvider;

    @Column(name = "provider_user_id")
    private String providerUserId;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @OneToOne(mappedBy = "user")
    private UserProfile userProfile;

    public static User createLocal(String email, String encodedPassword) {
        User user = new User();
        user.email = email;
        user.password = encodedPassword;
        user.status = UserStatus.ACTIVE;
        user.authProvider = UserAuthProvider.LOCAL;
        user.providerUserId = null;
        return user;
    }

    public static User createSocial(UserAuthProvider provider, String providerUserId, String email) {
        User user = new User();
        user.email = email;
        user.password = null;
        user.status = UserStatus.ACTIVE;
        user.authProvider = provider;
        user.providerUserId = providerUserId;
        return user;
    }
}