package com.recipemate.domain.comment.service;

import com.recipemate.domain.comment.dto.CommentResponse;
import com.recipemate.domain.comment.dto.CreateCommentRequest;
import com.recipemate.domain.comment.dto.UpdateCommentRequest;
import com.recipemate.domain.comment.entity.Comment;
import com.recipemate.domain.comment.repository.CommentRepository;
import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.repository.GroupBuyRepository;
import com.recipemate.domain.notification.service.NotificationService;
import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.post.repository.PostRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.*;
import com.recipemate.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 테스트")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupBuyRepository groupBuyRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CommentService commentService;

    private User author;
    private User anotherUser;
    private GroupBuy groupBuy;
    private Post post;

    @BeforeEach
    void setUp() throws Exception {
        author = User.builder()
                .email("author@example.com")
                .password("encodedPassword123")
                .nickname("작성자")
                .phoneNumber("010-1234-5678")
                .role(UserRole.USER)
                .build();
        setUserId(author, 1L);

        anotherUser = User.builder()
                .email("another@example.com")
                .password("encodedPassword123")
                .nickname("다른사용자")
                .phoneNumber("010-9876-5432")
                .role(UserRole.USER)
                .build();
        setUserId(anotherUser, 2L);

        groupBuy = GroupBuy.builder()
                .host(author)
                .title("파스타 재료 공구")
                .content("파스타 재료 같이 사실 분")
                .category("식재료")
                .totalPrice(50000)
                .targetHeadcount(5)
                .currentHeadcount(1)
                .deadline(LocalDateTime.now().plusDays(7))
                .deliveryMethod(DeliveryMethod.BOTH)
                .meetupLocation("서울대 정문")
                .parcelFee(3000)
                .isParticipantListPublic(true)
                .status(GroupBuyStatus.RECRUITING)
                .build();
        setGroupBuyId(groupBuy, 1L);

        post = Post.builder()
                .author(author)
                .title("공구 후기")
                .content("지난번 공구 좋았어요")
                .category(PostCategory.REVIEW)
                .viewCount(0)
                .build();
        setPostId(post, 1L);
    }

    // === Helper Methods ===

    private void setUserId(User user, Long id) throws Exception {
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
    }

    private void setGroupBuyId(GroupBuy groupBuy, Long id) throws Exception {
        Field idField = GroupBuy.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(groupBuy, id);
    }

    private void setPostId(Post post, Long id) throws Exception {
        Field idField = Post.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(post, id);
    }

    private void setCommentId(Comment comment, Long id) throws Exception {
        Field idField = Comment.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(comment, id);
    }

    private void setCreatedAt(Comment comment, LocalDateTime createdAt) throws Exception {
        Field createdAtField = Comment.class.getSuperclass().getDeclaredField("createdAt");
        createdAtField.setAccessible(true);
        createdAtField.set(comment, createdAt);
    }

    // === 댓글 작성 테스트 ===

    @Test
    @DisplayName("공구에 댓글을 작성할 수 있다")
    void createCommentOnGroupBuy_success() throws Exception {
        // given
        CreateCommentRequest request = CreateCommentRequest.builder()
                .targetType(EntityType.GROUP_BUY)
                .targetId(1L)
                .content("참여하고 싶습니다!")
                .type(CommentType.Q_AND_A)
                .build();

        Comment savedComment = Comment.builder()
                .author(author)
                .groupBuy(groupBuy)
                .content(request.getContent())
                .type(request.getType())
                .build();
        setCommentId(savedComment, 1L);
        setCreatedAt(savedComment, LocalDateTime.now());

        given(userRepository.findById(author.getId())).willReturn(Optional.of(author));
        given(groupBuyRepository.findById(1L)).willReturn(Optional.of(groupBuy));
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        // when
        CommentResponse response = commentService.createComment(author.getId(), request);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAuthorId()).isEqualTo(author.getId());
        assertThat(response.getAuthorNickname()).isEqualTo("작성자");
        assertThat(response.getContent()).isEqualTo("참여하고 싶습니다!");
        assertThat(response.getType()).isEqualTo(CommentType.Q_AND_A);
        assertThat(response.getParentId()).isNull();

        verify(userRepository).findById(author.getId());
        verify(groupBuyRepository).findById(1L);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("게시글에 댓글을 작성할 수 있다")
    void createCommentOnPost_success() throws Exception {
        // given
        CreateCommentRequest request = CreateCommentRequest.builder()
                .targetType(EntityType.POST)
                .targetId(1L)
                .content("좋은 정보 감사합니다!")
                .type(CommentType.GENERAL)
                .build();

        Comment savedComment = Comment.builder()
                .author(author)
                .post(post)
                .content(request.getContent())
                .type(request.getType())
                .build();
        setCommentId(savedComment, 1L);
        setCreatedAt(savedComment, LocalDateTime.now());

        given(userRepository.findById(author.getId())).willReturn(Optional.of(author));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        // when
        CommentResponse response = commentService.createComment(author.getId(), request);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAuthorId()).isEqualTo(author.getId());
        assertThat(response.getContent()).isEqualTo("좋은 정보 감사합니다!");
        assertThat(response.getType()).isEqualTo(CommentType.GENERAL);

        verify(userRepository).findById(author.getId());
        verify(postRepository).findById(1L);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("대댓글을 작성할 수 있다")
    void createReplyComment_success() throws Exception {
        // given
        Comment parentComment = Comment.builder()
                .author(author)
                .post(post)
                .content("원댓글입니다")
                .type(CommentType.GENERAL)
                .build();
        setCommentId(parentComment, 1L);

        CreateCommentRequest request = CreateCommentRequest.builder()
                .targetType(EntityType.POST)
                .targetId(1L)
                .content("답변드립니다")
                .type(CommentType.GENERAL)
                .parentId(1L)
                .build();

        Comment savedComment = Comment.builder()
                .author(anotherUser)
                .post(post)
                .parent(parentComment)
                .content(request.getContent())
                .type(request.getType())
                .build();
        setCommentId(savedComment, 2L);
        setCreatedAt(savedComment, LocalDateTime.now());

        given(userRepository.findById(anotherUser.getId())).willReturn(Optional.of(anotherUser));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(commentRepository.findById(1L)).willReturn(Optional.of(parentComment));
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        // when
        CommentResponse response = commentService.createComment(anotherUser.getId(), request);

        // then
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getAuthorId()).isEqualTo(anotherUser.getId());
        assertThat(response.getContent()).isEqualTo("답변드립니다");
        assertThat(response.getParentId()).isEqualTo(1L);

        verify(commentRepository).findById(1L);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자는 댓글을 작성할 수 없다")
    void createComment_userNotFound() {
        // given
        Long nonExistentUserId = 999L;
        CreateCommentRequest request = CreateCommentRequest.builder()
                .targetType(EntityType.POST)
                .targetId(1L)
                .content("댓글 내용")
                .type(CommentType.GENERAL)
                .build();

        given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.createComment(nonExistentUserId, request))
                .isInstanceOf(CustomException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(userRepository).findById(nonExistentUserId);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("존재하지 않는 공구에는 댓글을 작성할 수 없다")
    void createComment_groupBuyNotFound() {
        // given
        Long nonExistentGroupBuyId = 999L;
        CreateCommentRequest request = CreateCommentRequest.builder()
                .targetType(EntityType.GROUP_BUY)
                .targetId(nonExistentGroupBuyId)
                .content("댓글 내용")
                .type(CommentType.Q_AND_A)
                .build();

        given(userRepository.findById(author.getId())).willReturn(Optional.of(author));
        given(groupBuyRepository.findById(nonExistentGroupBuyId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.createComment(author.getId(), request))
                .isInstanceOf(CustomException.class)
                .hasMessage("공동구매를 찾을 수 없습니다.");

        verify(groupBuyRepository).findById(nonExistentGroupBuyId);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("존재하지 않는 게시글에는 댓글을 작성할 수 없다")
    void createComment_postNotFound() {
        // given
        Long nonExistentPostId = 999L;
        CreateCommentRequest request = CreateCommentRequest.builder()
                .targetType(EntityType.POST)
                .targetId(nonExistentPostId)
                .content("댓글 내용")
                .type(CommentType.GENERAL)
                .build();

        given(userRepository.findById(author.getId())).willReturn(Optional.of(author));
        given(postRepository.findById(nonExistentPostId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.createComment(author.getId(), request))
                .isInstanceOf(CustomException.class)
                .hasMessage("게시글을 찾을 수 없습니다.");

        verify(postRepository).findById(nonExistentPostId);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("대댓글의 대댓글은 작성할 수 없다 (깊이 제한)")
    void createComment_replyDepthExceeded() throws Exception {
        // given
        Comment parentComment = Comment.builder()
                .author(author)
                .post(post)
                .content("원댓글")
                .type(CommentType.GENERAL)
                .build();
        setCommentId(parentComment, 1L);

        Comment replyComment = Comment.builder()
                .author(anotherUser)
                .post(post)
                .parent(parentComment)
                .content("대댓글")
                .type(CommentType.GENERAL)
                .build();
        setCommentId(replyComment, 2L);

        CreateCommentRequest request = CreateCommentRequest.builder()
                .targetType(EntityType.POST)
                .targetId(1L)
                .content("대댓글의 대댓글 시도")
                .type(CommentType.GENERAL)
                .parentId(2L)
                .build();

        given(userRepository.findById(author.getId())).willReturn(Optional.of(author));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(commentRepository.findById(2L)).willReturn(Optional.of(replyComment));

        // when & then
        assertThatThrownBy(() -> commentService.createComment(author.getId(), request))
                .isInstanceOf(CustomException.class)
                .hasMessage("대댓글의 대댓글은 작성할 수 없습니다.");

        verify(commentRepository, never()).save(any(Comment.class));
    }

    // === 댓글 수정 테스트 ===

    @Test
    @DisplayName("본인이 작성한 댓글을 수정할 수 있다")
    void updateComment_success() throws Exception {
        // given
        Comment comment = Comment.builder()
                .author(author)
                .post(post)
                .content("원래 내용")
                .type(CommentType.GENERAL)
                .build();
        setCommentId(comment, 1L);
        setCreatedAt(comment, LocalDateTime.now());

        UpdateCommentRequest request = UpdateCommentRequest.builder()
                .content("수정된 내용")
                .build();

        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // when
        CommentResponse response = commentService.updateComment(author.getId(), 1L, request);

        // then
        assertThat(response.getContent()).isEqualTo("수정된 내용");
        assertThat(comment.getContent()).isEqualTo("수정된 내용");

        verify(commentRepository).findById(1L);
    }

    @Test
    @DisplayName("다른 사용자의 댓글은 수정할 수 없다")
    void updateComment_notAuthor() throws Exception {
        // given
        Comment comment = Comment.builder()
                .author(author)
                .post(post)
                .content("원래 내용")
                .type(CommentType.GENERAL)
                .build();
        setCommentId(comment, 1L);

        UpdateCommentRequest request = UpdateCommentRequest.builder()
                .content("수정 시도")
                .build();

        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(anotherUser.getId(), 1L, request))
                .isInstanceOf(CustomException.class)
                .hasMessage("댓글 수정 권한이 없습니다.");

        assertThat(comment.getContent()).isEqualTo("원래 내용");
        verify(commentRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 댓글은 수정할 수 없다")
    void updateComment_commentNotFound() {
        // given
        Long nonExistentCommentId = 999L;
        UpdateCommentRequest request = UpdateCommentRequest.builder()
                .content("수정 시도")
                .build();

        given(commentRepository.findById(nonExistentCommentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(author.getId(), nonExistentCommentId, request))
                .isInstanceOf(CustomException.class)
                .hasMessage("댓글을 찾을 수 없습니다.");

        verify(commentRepository).findById(nonExistentCommentId);
    }

    @Test
    @DisplayName("삭제된 댓글은 수정할 수 없다")
    void updateComment_deletedComment() throws Exception {
        // given
        Comment comment = Comment.builder()
                .author(author)
                .post(post)
                .content("삭제된 댓글입니다")
                .type(CommentType.GENERAL)
                .build();
        setCommentId(comment, 1L);

        Field deletedAtField = Comment.class.getSuperclass().getDeclaredField("deletedAt");
        deletedAtField.setAccessible(true);
        deletedAtField.set(comment, LocalDateTime.now());

        UpdateCommentRequest request = UpdateCommentRequest.builder()
                .content("수정 시도")
                .build();

        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(author.getId(), 1L, request))
                .isInstanceOf(CustomException.class)
                .hasMessage("댓글을 찾을 수 없습니다.");

        verify(commentRepository).findById(1L);
    }

    // === 댓글 삭제 테스트 ===

    @Test
    @DisplayName("본인이 작성한 댓글을 삭제할 수 있다")
    void deleteComment_success() throws Exception {
        // given
        Comment comment = Comment.builder()
                .author(author)
                .post(post)
                .content("삭제할 댓글")
                .type(CommentType.GENERAL)
                .build();
        setCommentId(comment, 1L);

        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // when
        commentService.deleteComment(author.getId(), 1L);

        // then
        assertThat(comment.getContent()).isEqualTo("삭제된 댓글입니다");
        assertThat(comment.getDeletedAt()).isNotNull();

        verify(commentRepository).findById(1L);
    }

    @Test
    @DisplayName("다른 사용자의 댓글은 삭제할 수 없다")
    void deleteComment_notAuthor() throws Exception {
        // given
        Comment comment = Comment.builder()
                .author(author)
                .post(post)
                .content("원래 내용")
                .type(CommentType.GENERAL)
                .build();
        setCommentId(comment, 1L);

        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(anotherUser.getId(), 1L))
                .isInstanceOf(CustomException.class)
                .hasMessage("댓글 삭제 권한이 없습니다.");

        assertThat(comment.getDeletedAt()).isNull();
        assertThat(comment.getContent()).isEqualTo("원래 내용");
        verify(commentRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 댓글은 삭제할 수 없다")
    void deleteComment_commentNotFound() {
        // given
        Long nonExistentCommentId = 999L;
        given(commentRepository.findById(nonExistentCommentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(author.getId(), nonExistentCommentId))
                .isInstanceOf(CustomException.class)
                .hasMessage("댓글을 찾을 수 없습니다.");

        verify(commentRepository).findById(nonExistentCommentId);
    }

    @Test
    @DisplayName("이미 삭제된 댓글은 다시 삭제할 수 없다")
    void deleteComment_alreadyDeleted() throws Exception {
        // given
        Comment comment = Comment.builder()
                .author(author)
                .post(post)
                .content("삭제된 댓글입니다")
                .type(CommentType.GENERAL)
                .build();
        setCommentId(comment, 1L);

        Field deletedAtField = Comment.class.getSuperclass().getDeclaredField("deletedAt");
        deletedAtField.setAccessible(true);
        deletedAtField.set(comment, LocalDateTime.now());

        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(author.getId(), 1L))
                .isInstanceOf(CustomException.class)
                .hasMessage("댓글을 찾을 수 없습니다.");

        verify(commentRepository).findById(1L);
    }
}
