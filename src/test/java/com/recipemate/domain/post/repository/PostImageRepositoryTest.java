//package com.recipemate.domain.post.repository;
//
//import com.recipemate.domain.post.entity.Post;
//import com.recipemate.domain.post.entity.PostImage;
//import com.recipemate.domain.user.entity.User;
//import com.recipemate.domain.user.repository.UserRepository;
//import com.recipemate.global.common.PostCategory;
//import com.recipemate.global.common.UserRole;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//@Import(com.recipemate.global.config.QueryDslConfig.class)
//@ActiveProfiles("test")
//@DisplayName("PostImageRepository 통합 테스트")
//class PostImageRepositoryTest {
//
//    @Autowired
//    private PostImageRepository postImageRepository;
//
//    @Autowired
//    private PostRepository postRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    private Post post;
//
//    @BeforeEach
//    void setUp() {
//        userRepository.deleteAll();
//        postRepository.deleteAll();
//        postImageRepository.deleteAll();
//
//        User author = User.builder()
//                .email("author@example.com")
//                .password("password")
//                .nickname("test-author")
//                .phoneNumber("010-1234-5678")
//                .role(UserRole.USER)
//                .build();
//        userRepository.save(author);
//
//        post = Post.builder()
//                .author(author)
//                .title("Test Post")
//                .content("This is a test post.")
//                .category(PostCategory.TIPS)
//                .viewCount(0)
//                .build();
//        post = postRepository.save(post);
//    }
//
//    @Test
//    @DisplayName("게시글 ID로 이미지 목록을 displayOrder 오름차순으로 조회한다.")
//    void findByPostIdOrderByDisplayOrderAsc() {
//        // given
//        PostImage image1 = createImage(post, "url1", 1);
//        PostImage image2 = createImage(post, "url2", 0);
//        PostImage image3 = createImage(post, "url3", 2);
//        postImageRepository.saveAll(List.of(image1, image2, image3));
//
//        // when
//        List<PostImage> result = postImageRepository.findByPostIdOrderByDisplayOrderAsc(post.getId());
//
//        // then
//        assertThat(result).hasSize(3);
//        assertThat(result.get(0).getDisplayOrder()).isEqualTo(0);
//        assertThat(result.get(0).getImageUrl()).isEqualTo("url2");
//        assertThat(result.get(1).getDisplayOrder()).isEqualTo(1);
//        assertThat(result.get(1).getImageUrl()).isEqualTo("url1");
//        assertThat(result.get(2).getDisplayOrder()).isEqualTo(2);
//        assertThat(result.get(2).getImageUrl()).isEqualTo("url3");
//    }
//
//    @Test
//    @DisplayName("게시글 엔티티로 이미지 목록을 displayOrder 오름차순으로 조회한다.")
//    void findByPostOrderByDisplayOrderAsc() {
//        // given
//        PostImage image1 = createImage(post, "url1", 2);
//        PostImage image2 = createImage(post, "url2", 1);
//        PostImage image3 = createImage(post, "url3", 0);
//        postImageRepository.saveAll(List.of(image1, image2, image3));
//
//        // when
//        List<PostImage> result = postImageRepository.findByPostOrderByDisplayOrderAsc(post);
//
//        // then
//        assertThat(result).hasSize(3);
//        assertThat(result.get(0).getDisplayOrder()).isEqualTo(0);
//        assertThat(result.get(0).getImageUrl()).isEqualTo("url3");
//        assertThat(result.get(1).getDisplayOrder()).isEqualTo(1);
//        assertThat(result.get(1).getImageUrl()).isEqualTo("url2");
//        assertThat(result.get(2).getDisplayOrder()).isEqualTo(2);
//        assertThat(result.get(2).getImageUrl()).isEqualTo("url1");
//    }
//
//    private PostImage createImage(Post post, String imageUrl, int displayOrder) {
//        return PostImage.builder()
//                .post(post)
//                .imageUrl(imageUrl)
//                .displayOrder(displayOrder)
//                .build();
//    }
//}
