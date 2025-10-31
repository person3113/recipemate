package com.recipemate.domain.post.entity;

import com.recipemate.domain.post.repository.PostRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.PostCategory;
import com.recipemate.global.common.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(com.recipemate.global.config.QueryDslConfig.class)
class PostImageTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private User author;
    private Post post;

    @BeforeEach
    void setUp() {
        author = User.builder()
                .email("author@example.com")
                .password("password")
                .nickname("author")
                .phoneNumber("010-1234-5678")
                .mannerTemperature(36.5)
                .role(UserRole.USER)
                .build();
        userRepository.save(author);

        post = Post.builder()
                .author(author)
                .title("Test Post")
                .content("Test Content")
                .category(PostCategory.TIPS)
                .viewCount(0)
                .build();
        postRepository.save(post);
    }

    @Test
    @DisplayName("성공: PostImage 엔티티를 저장한다")
    void savePostImage_success() {
        // given
        PostImage image = PostImage.builder()
                .post(post)
                .imageUrl("https://example.com/image.jpg")
                .displayOrder(1)
                .build();

        // when
        entityManager.persist(image);
        entityManager.flush();
        entityManager.clear();

        // then
        PostImage foundImage = entityManager.find(PostImage.class, image.getId());
        assertThat(foundImage).isNotNull();
        assertThat(foundImage.getImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(foundImage.getDisplayOrder()).isEqualTo(1);
        assertThat(foundImage.getPost().getId()).isEqualTo(post.getId());
    }

    @Test
    @DisplayName("실패: Post 없이 PostImage를 저장하면 예외가 발생한다")
    void createPostImage_fail_withoutPost() {
        // given
        PostImage image = PostImage.builder()
                .post(null) // Post를 null로 설정
                .imageUrl("https://example.com/image.jpg")
                .displayOrder(1)
                .build();

        // when & then
        assertThatThrownBy(() -> {
            entityManager.persist(image);
            entityManager.flush();
        }).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("실패: 같은 게시글 내에서 displayOrder가 중복되면 예외가 발생한다")
    void createPostImage_fail_withDuplicateDisplayOrder() {
        // given
        PostImage image1 = PostImage.builder()
                .post(post)
                .imageUrl("https://example.com/image1.jpg")
                .displayOrder(1)
                .build();
        entityManager.persist(image1);

        PostImage image2 = PostImage.builder()
                .post(post)
                .imageUrl("https://example.com/image2.jpg")
                .displayOrder(1) // 중복된 displayOrder
                .build();

        // when & then
        assertThatThrownBy(() -> {
            entityManager.persist(image2);
            entityManager.flush(); // 제약조건 위반은 flush 시점에 발생
        }).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("성공: 다른 게시글이면 같은 displayOrder를 가질 수 있다")
    void createPostImage_success_withSameDisplayOrderInDifferentPost() {
        // given
        Post anotherPost = Post.builder()
                .author(author)
                .title("Another Post")
                .content("Another Content")
                .category(PostCategory.FREE)
                .viewCount(0)
                .build();
        postRepository.save(anotherPost);

        PostImage image1 = PostImage.builder()
                .post(post)
                .imageUrl("https://example.com/image1.jpg")
                .displayOrder(1)
                .build();
        entityManager.persist(image1);

        PostImage image2 = PostImage.builder()
                .post(anotherPost)
                .imageUrl("https://example.com/image2.jpg")
                .displayOrder(1) // 다른 게시글이므로 같은 displayOrder 가능
                .build();

        // when
        entityManager.persist(image2);
        entityManager.flush();
        entityManager.clear();

        // then
        PostImage foundImage1 = entityManager.find(PostImage.class, image1.getId());
        PostImage foundImage2 = entityManager.find(PostImage.class, image2.getId());
        assertThat(foundImage1).isNotNull();
        assertThat(foundImage2).isNotNull();
        assertThat(foundImage1.getDisplayOrder()).isEqualTo(1);
        assertThat(foundImage2.getDisplayOrder()).isEqualTo(1);
    }
}
