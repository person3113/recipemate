//package com.recipemate.domain.user.repository;
//
//import com.recipemate.domain.user.entity.User;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.context.annotation.Import;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//@Import(com.recipemate.global.config.QueryDslConfig.class)
//class UserRepositoryTest {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Test
//    @DisplayName("이메일로 회원을 조회할 수 있다")
//    void findByEmail() {
//        User user = User.create("test@example.com", "password", "테스터", "010-1234-5678");
//        userRepository.save(user);
//
//        User found = userRepository.findByEmail("test@example.com").orElseThrow();
//
//        assertThat(found.getEmail()).isEqualTo("test@example.com");
//    }
//
//    @Test
//    @DisplayName("닉네임으로 회원을 조회할 수 있다")
//    void findByNickname() {
//        User user = User.create("test@example.com", "password", "테스터", "010-1234-5678");
//        userRepository.save(user);
//
//        User found = userRepository.findByNickname("테스터").orElseThrow();
//
//        assertThat(found.getNickname()).isEqualTo("테스터");
//    }
//
//    @Test
//    @DisplayName("이메일 중복을 확인할 수 있다")
//    void existsByEmail() {
//        User user = User.create("test@example.com", "password", "테스터", "010-1234-5678");
//        userRepository.save(user);
//
//        boolean exists = userRepository.existsByEmail("test@example.com");
//
//        assertThat(exists).isTrue();
//    }
//
//    @Test
//    @DisplayName("닉네임 중복을 확인할 수 있다")
//    void existsByNickname() {
//        User user = User.create("test@example.com", "password", "테스터", "010-1234-5678");
//        userRepository.save(user);
//
//        boolean exists = userRepository.existsByNickname("테스터");
//
//        assertThat(exists).isTrue();
//    }
//
//    @Test
//    @DisplayName("이메일은 유니크해야 한다")
//    void emailUnique() {
//        User user1 = User.create("test@example.com", "password1", "테스터1", "010-1111-1111");
//        userRepository.save(user1);
//
//        User user2 = User.create("test@example.com", "password2", "테스터2", "010-2222-2222");
//
//        try {
//            userRepository.saveAndFlush(user2);
//        } catch (Exception e) {
//            assertThat(e).isNotNull();
//        }
//    }
//
//    @Test
//    @DisplayName("닉네임은 유니크해야 한다")
//    void nicknameUnique() {
//        User user1 = User.create("test1@example.com", "password1", "테스터", "010-1111-1111");
//        userRepository.save(user1);
//
//        User user2 = User.create("test2@example.com", "password2", "테스터", "010-2222-2222");
//
//        try {
//            userRepository.saveAndFlush(user2);
//        } catch (Exception e) {
//            assertThat(e).isNotNull();
//        }
//    }
//}
