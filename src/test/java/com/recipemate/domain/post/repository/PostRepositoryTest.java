package com.recipemate.domain.post.repository;

import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.config.QueryDslConfig;
import com.recipemate.global.common.PostCategory;
import com.recipemate.global.common.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
@DisplayName("PostRepository 통합 테스트")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User author;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        postRepository.deleteAll();

        author = User.builder()
                .email("author@example.com")
                .password("password")
                .nickname("test-author")
                .phoneNumber("010-1234-5678")
                .role(UserRole.USER)
                .build();
        userRepository.save(author);
    }

    @Test
    @DisplayName("게시글을 저장하고 ID로 조회할 수 있다.")
    void saveAndFindById() {
        // given
        Post post = Post.builder()
                .author(author)
                .title("테스트 제목")
                .content("테스트 내용")
                .category(PostCategory.FREE)
                .build();

        // when
        Post savedPost = postRepository.save(post);
        Post foundPost = postRepository.findById(savedPost.getId()).orElse(null);

        // then
        assertThat(foundPost).isNotNull();
        assertThat(foundPost.getId()).isEqualTo(savedPost.getId());
        assertThat(foundPost.getTitle()).isEqualTo("테스트 제목");
        assertThat(foundPost.getAuthor().getNickname()).isEqualTo("test-author");
    }

    @Test
    @DisplayName("카테고리로 게시글 목록을 최신순으로 조회한다.")
    void findByCategoryOrderByCreatedAtDesc() {
        // given
        Post post1 = createPost("자유글 1", PostCategory.FREE);
        Post post2 = createPost("꿀팁글 1", PostCategory.TIPS);
        Post post3 = createPost("자유글 2", PostCategory.FREE);
        postRepository.saveAll(List.of(post1, post2, post3));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Post> freePosts = postRepository.findByCategoryOrderByCreatedAtDesc(PostCategory.FREE, pageable);
        Page<Post> tipsPosts = postRepository.findByCategoryOrderByCreatedAtDesc(PostCategory.TIPS, pageable);

        // then
        assertThat(freePosts.getContent()).hasSize(2);
        assertThat(freePosts.getContent()).extracting(Post::getTitle).containsExactly("자유글 2", "자유글 1");
        assertThat(tipsPosts.getContent()).hasSize(1);
        assertThat(tipsPosts.getContent().get(0).getTitle()).isEqualTo("꿀팁글 1");
    }

    @Test
    @DisplayName("작성자 ID로 게시글 목록을 조회한다.")
    void findByAuthorId() {
        // given
        User anotherAuthor = User.builder()
                .email("another@example.com")
                .password("password")
                .nickname("another-author")
                .phoneNumber("010-8765-4321")
                .role(UserRole.USER)
                .build();
        userRepository.save(anotherAuthor);

        Post post1 = createPost("내글 1", author);
        Post post2 = createPost("남의글 1", anotherAuthor);
        Post post3 = createPost("내글 2", author);
        postRepository.saveAll(List.of(post1, post2, post3));

        // when
        List<Post> myPosts = postRepository.findByAuthorId(author.getId());

        // then
        assertThat(myPosts).hasSize(2);
        assertThat(myPosts).extracting(Post::getTitle).containsExactlyInAnyOrder("내글 1", "내글 2");
        assertThat(myPosts).allMatch(p -> p.getAuthor().equals(author));
    }

    private Post createPost(String title, PostCategory category) {
        return createPost(title, category, this.author);
    }

    private Post createPost(String title, User author) {
        return createPost(title, PostCategory.FREE, author);
    }

    private Post createPost(String title, PostCategory category, User author) {
        return Post.builder()
                .author(author)
                .title(title)
                .content("내용 " + title)
                .category(category)
                .build();
    }
}
