package com.hooong.simpleMember.Repository;

        import com.hooong.simpleMember.Domain.Member;
        import org.springframework.data.jpa.repository.JpaRepository;

        import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByusername(String username);
}
