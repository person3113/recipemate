package com.recipemate.domain.post.entity;

import com.recipemate.domain.user.entity.User;
import com.recipemate.global.common.PostCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Post 엔티티 테스트")
class PostTest {

    @Test
    @DisplayName("게시글을 정상적으로 생성한다")
    void createPost() {
        // given
        User author = User.builder()
                .id(1L)
                .nickname("testuser")
                .build();
        String title = "테스트 제목";
        String content = "테스트 내용입니다.";
        PostCategory category = PostCategory.FREE;

        // when
        Post post = Post.builder()
                .author(author)
                .title(title)
                .content(content)
                .category(category)
                .build();

        // then
        assertThat(post).isNotNull();
        assertThat(post.getAuthor()).isEqualTo(author);
        assertThat(post.getTitle()).isEqualTo(title);
        assertThat(post.getContent()).isEqualTo(content);
        assertThat(post.getCategory()).isEqualTo(category);
        assertThat(post.getViewCount()).isZero(); // 기본값 0 확인
    }

    @Test
    @DisplayName("조회수를 1 증가시킨다")
    void increaseViewCount() {
        // given
        Post post = Post.builder()
                .title("any title")
                .content("any content")
                .category(PostCategory.TIPS)
                .build();

        int initialViewCount = post.getViewCount();

        // when
        post.increaseViewCount();

        // then
        assertThat(post.getViewCount()).isEqualTo(initialViewCount + 1);
    }
}
