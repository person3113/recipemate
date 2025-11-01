package com.recipemate.domain.comment;

import com.recipemate.domain.comment.entity.Comment;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.user.entity.User;
import com.recipemate.global.common.CommentType;
import com.recipemate.global.common.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Comment 엔티티 테스트")
class CommentTest {

    @Test
    @DisplayName("게시글에 댓글을 생성할 수 있다")
    void createCommentOnPost() {
        // given
        User author = createUser("test@example.com", "테스터");
        Post post = createPost(author, "테스트 게시글");

        // when
        Comment comment = Comment.builder()
                .author(author)
                .post(post)
                .content("좋은 정보 감사합니다!")
                .type(CommentType.GENERAL)
                .build();

        // then
        assertThat(comment.getAuthor()).isEqualTo(author);
        assertThat(comment.getPost()).isEqualTo(post);
        assertThat(comment.getGroupBuy()).isNull();
        assertThat(comment.getContent()).isEqualTo("좋은 정보 감사합니다!");
        assertThat(comment.getType()).isEqualTo(CommentType.GENERAL);
        assertThat(comment.getParent()).isNull();
    }

    @Test
    @DisplayName("공구에 댓글을 생성할 수 있다")
    void createCommentOnGroupBuy() {
        // given
        User author = createUser("test@example.com", "테스터");
        GroupBuy groupBuy = createGroupBuy(author, "파스타 재료 공구");

        // when
        Comment comment = Comment.builder()
                .author(author)
                .groupBuy(groupBuy)
                .content("참여하고 싶습니다!")
                .type(CommentType.Q_AND_A)
                .build();

        // then
        assertThat(comment.getAuthor()).isEqualTo(author);
        assertThat(comment.getGroupBuy()).isEqualTo(groupBuy);
        assertThat(comment.getPost()).isNull();
        assertThat(comment.getContent()).isEqualTo("참여하고 싶습니다!");
        assertThat(comment.getType()).isEqualTo(CommentType.Q_AND_A);
    }

    @Test
    @DisplayName("대댓글(답글)을 생성할 수 있다")
    void createReplyComment() {
        // given
        User author = createUser("test@example.com", "테스터");
        User replyAuthor = createUser("reply@example.com", "답변자");
        Post post = createPost(author, "테스트 게시글");
        
        Comment parentComment = Comment.builder()
                .author(author)
                .post(post)
                .content("질문이 있습니다")
                .type(CommentType.GENERAL)
                .build();

        // when
        Comment replyComment = Comment.builder()
                .author(replyAuthor)
                .post(post)
                .parent(parentComment)
                .content("답변드립니다")
                .type(CommentType.GENERAL)
                .build();

        // then
        assertThat(replyComment.getParent()).isEqualTo(parentComment);
        assertThat(replyComment.getAuthor()).isEqualTo(replyAuthor);
        assertThat(replyComment.getPost()).isEqualTo(post);
    }

    @Test
    @DisplayName("댓글은 게시글 또는 공구 중 정확히 하나에만 연결되어야 한다 - 둘 다 NULL")
    void validateCommentTargetBothNull() {
        // given
        User author = createUser("test@example.com", "테스터");

        // when & then
        assertThatThrownBy(() -> {
            Comment comment = Comment.builder()
                    .author(author)
                    .content("댓글")
                    .type(CommentType.GENERAL)
                    .build();
            comment.validateTarget();
        })
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("댓글은 게시글 또는 공구 중 정확히 하나에만 연결되어야 합니다");
    }

    @Test
    @DisplayName("댓글은 게시글 또는 공구 중 정확히 하나에만 연결되어야 한다 - 둘 다 NOT NULL")
    void validateCommentTargetBothNotNull() {
        // given
        User author = createUser("test@example.com", "테스터");
        Post post = createPost(author, "테스트 게시글");
        GroupBuy groupBuy = createGroupBuy(author, "파스타 재료 공구");

        // when & then
        assertThatThrownBy(() -> {
            Comment comment = Comment.builder()
                    .author(author)
                    .post(post)
                    .groupBuy(groupBuy)
                    .content("댓글")
                    .type(CommentType.GENERAL)
                    .build();
            comment.validateTarget();
        })
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("댓글은 게시글 또는 공구 중 정확히 하나에만 연결되어야 합니다");
    }

    @Test
    @DisplayName("Q&A 타입 댓글을 생성할 수 있다")
    void createQAndAComment() {
        // given
        User author = createUser("test@example.com", "테스터");
        GroupBuy groupBuy = createGroupBuy(author, "파스타 재료 공구");

        // when
        Comment comment = Comment.builder()
                .author(author)
                .groupBuy(groupBuy)
                .content("배송은 언제 하나요?")
                .type(CommentType.Q_AND_A)
                .build();

        // then
        assertThat(comment.getType()).isEqualTo(CommentType.Q_AND_A);
    }

    @Test
    @DisplayName("일반 타입 댓글을 생성할 수 있다")
    void createGeneralComment() {
        // given
        User author = createUser("test@example.com", "테스터");
        Post post = createPost(author, "테스트 게시글");

        // when
        Comment comment = Comment.builder()
                .author(author)
                .post(post)
                .content("좋은 글 감사합니다!")
                .type(CommentType.GENERAL)
                .build();

        // then
        assertThat(comment.getType()).isEqualTo(CommentType.GENERAL);
    }

    @Test
    @DisplayName("댓글 내용을 수정할 수 있다")
    void updateCommentContent() {
        // given
        User author = createUser("test@example.com", "테스터");
        Post post = createPost(author, "테스트 게시글");
        Comment comment = Comment.builder()
                .author(author)
                .post(post)
                .content("원래 내용")
                .type(CommentType.GENERAL)
                .build();

        // when
        comment.updateContent("수정된 내용");

        // then
        assertThat(comment.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("댓글을 소프트 삭제할 수 있다")
    void softDeleteComment() {
        // given
        User author = createUser("test@example.com", "테스터");
        Post post = createPost(author, "테스트 게시글");
        Comment comment = Comment.builder()
                .author(author)
                .post(post)
                .content("삭제될 댓글")
                .type(CommentType.GENERAL)
                .build();

        // when
        comment.markAsDeleted();

        // then
        assertThat(comment.getContent()).isEqualTo("삭제된 댓글입니다");
        assertThat(comment.getDeletedAt()).isNotNull();
    }

    // === Helper Methods ===

    private User createUser(String email, String nickname) {
        return User.builder()
                .email(email)
                .password("password123!")
                .nickname(nickname)
                .phoneNumber("010-1234-5678")
                .mannerTemperature(36.5)
                .role(UserRole.USER)
                .build();
    }

    private Post createPost(User author, String title) {
        return Post.builder()
                .author(author)
                .title(title)
                .content("테스트 내용")
                .category(com.recipemate.global.common.PostCategory.FREE)
                .viewCount(0)
                .build();
    }

    private GroupBuy createGroupBuy(User host, String title) {
        return GroupBuy.builder()
                .host(host)
                .title(title)
                .content("테스트 공구")
                .category("식재료")
                .totalPrice(50000)
                .targetHeadcount(5)
                .currentHeadcount(0)
                .deadline(LocalDateTime.now().plusDays(7))
                .deliveryMethod(com.recipemate.global.common.DeliveryMethod.BOTH)
                .meetupLocation("서울대 정문")
                .parcelFee(3000)
                .isParticipantListPublic(true)
                .status(com.recipemate.global.common.GroupBuyStatus.RECRUITING)
                .build();
    }
}
