package com.bready.server.user.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String nickname;

    private String bio;

    private String profileImageUrl;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 유저와의 연관관계 설정 (1:1)

    public static UserProfile create(User user, String nickname) {
        UserProfile profile = new UserProfile();
        profile.user = user;
        profile.nickname = nickname;
        // 양방향 연관관계 세팅 추가
        user.setUserProfile(profile);
        return profile;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeBio(String bio) {
        this.bio = bio;
    }

    public void changeProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
