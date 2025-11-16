package com.recipemate.domain.user.repository;

import com.recipemate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.nickname = :nickname AND u.deletedAt IS NULL")
    Optional<User> findByNickname(@Param("nickname") String nickname);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.nickname = :nickname AND u.deletedAt IS NULL")
    boolean existsByNickname(@Param("nickname") String nickname);

    // 소프트 삭제된 계정 포함 체크 (AdminUserInitializer용)
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email")
    boolean existsByEmailIncludingDeleted(@Param("email") String email);
    
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findById(@Param("id") Long id);

    // 소프트 삭제된 계정 포함 이메일로 사용자 조회
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailIncludingDeleted(@Param("email") String email);

    // 소프트 삭제된 계정 포함 닉네임으로 사용자 조회
    @Query("SELECT u FROM User u WHERE u.nickname = :nickname")
    Optional<User> findByNicknameIncludingDeleted(@Param("nickname") String nickname);

    // 이메일과 전화번호로 사용자 조회 (비밀번호 찾기용)
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.phoneNumber = :phoneNumber AND u.deletedAt IS NULL")
    Optional<User> findByEmailAndPhoneNumber(@Param("email") String email, @Param("phoneNumber") String phoneNumber);
}
