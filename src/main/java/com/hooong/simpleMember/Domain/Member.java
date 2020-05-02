package com.hooong.simpleMember.Domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@NoArgsConstructor(access=AccessLevel.PROTECTED)
@Getter
@Entity
public class Member {

    @Id
    @GeneratedValue
    private Long id;
    private String username;
    private String password;

    @Builder
    public Member(Long id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }
}
