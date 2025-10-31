package com.recipemate.domain.post.service;

import com.recipemate.domain.post.dto.CreatePostRequest;
import com.recipemate.domain.post.dto.PostResponse;
import com.recipemate.domain.post.dto.UpdatePostRequest;
import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.post.repository.PostRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.PostCategory;
import com.recipemate.global.common.UserRole;
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
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    private User author;
    private User anotherUser;

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
    }

    // Reflection utility methods to set IDs
    private void setUserId(User user, Long id) throws Exception {
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
    }

    private void setPostId(Post post, Long id) throws Exception {
        Field idField = Post.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(post, id);
    }

    private void setCreatedAt(Post post, LocalDateTime createdAt) throws Exception {
        Field createdAtField = Post.class.getSuperclass().getDeclaredField("createdAt");
        createdAtField.setAccessible(true);
        createdAtField.set(post, createdAt);
    }

    @Test
    @DisplayName("게시글 작성에 성공한다")
    void createPost_success() throws Exception {
        // given
        CreatePostRequest request = CreatePostRequest.builder()
                .title("맛있는 레시피 공유합니다")
                .content("이번에 만든 파스타 레시피가 정말 맛있어서 공유합니다.")
                .category(PostCategory.TIPS)
                .build();

        Post savedPost = Post.builder()
                .author(author)
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .viewCount(0)
                .build();
        setPostId(savedPost, 1L);
        setCreatedAt(savedPost, LocalDateTime.now());

        given(userRepository.findById(author.getId())).willReturn(Optional.of(author));
        given(postRepository.save(any(Post.class))).willReturn(savedPost);

        // when
        PostResponse response = postService.createPost(author.getId(), request);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("맛있는 레시피 공유합니다");
        assertThat(response.getContent()).isEqualTo("이번에 만든 파스타 레시피가 정말 맛있어서 공유합니다.");
        assertThat(response.getCategory()).isEqualTo(PostCategory.TIPS);
        assertThat(response.getViewCount()).isEqualTo(0);
        assertThat(response.getAuthorId()).isEqualTo(author.getId());
        assertThat(response.getAuthorNickname()).isEqualTo("작성자");

        verify(userRepository).findById(author.getId());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자는 게시글을 작성할 수 없다")
    void createPost_userNotFound() {
        // given
        Long nonExistentUserId = 999L;
        CreatePostRequest request = CreatePostRequest.builder()
                .title("테스트 게시글")
                .content("테스트 내용")
                .category(PostCategory.FREE)
                .build();

        given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.createPost(nonExistentUserId, request))
                .isInstanceOf(CustomException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(userRepository).findById(nonExistentUserId);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 상세 조회에 성공한다")
    void getPostDetail_success() throws Exception {
        // given
        Post post = Post.builder()
                .author(author)
                .title("공구 후기입니다")
                .content("지난번 참여한 공구 정말 좋았어요!")
                .category(PostCategory.REVIEW)
                .viewCount(5)
                .build();
        setPostId(post, 1L);
        setCreatedAt(post, LocalDateTime.now());

        given(postRepository.findByIdWithAuthor(1L)).willReturn(Optional.of(post));

        // when
        PostResponse response = postService.getPostDetail(1L);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("공구 후기입니다");
        assertThat(response.getContent()).isEqualTo("지난번 참여한 공구 정말 좋았어요!");
        assertThat(response.getCategory()).isEqualTo(PostCategory.REVIEW);
        assertThat(response.getViewCount()).isEqualTo(6); // increased
        assertThat(response.getAuthorId()).isEqualTo(author.getId());
        assertThat(response.getAuthorNickname()).isEqualTo("작성자");

        verify(postRepository).findByIdWithAuthor(1L);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 예외가 발생한다")
    void getPostDetail_postNotFound() {
        // given
        Long nonExistentPostId = 999L;
        given(postRepository.findByIdWithAuthor(nonExistentPostId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getPostDetail(nonExistentPostId))
                .isInstanceOf(CustomException.class)
                .hasMessage("게시글을 찾을 수 없습니다.");

        verify(postRepository).findByIdWithAuthor(nonExistentPostId);
    }

    @Test
    @DisplayName("삭제된 게시글 조회 시 예외가 발생한다")
    void getPostDetail_deletedPost() throws Exception {
        // given
        Post deletedPost = Post.builder()
                .author(author)
                .title("삭제된 게시글")
                .content("삭제된 내용")
                .category(PostCategory.FREE)
                .viewCount(0)
                .build();
        setPostId(deletedPost, 1L);
        
        Field deletedAtField = Post.class.getSuperclass().getDeclaredField("deletedAt");
        deletedAtField.setAccessible(true);
        deletedAtField.set(deletedPost, LocalDateTime.now());

        given(postRepository.findByIdWithAuthor(1L)).willReturn(Optional.of(deletedPost));

        // when & then
        assertThatThrownBy(() -> postService.getPostDetail(1L))
                .isInstanceOf(CustomException.class)
                .hasMessage("게시글을 찾을 수 없습니다.");

        verify(postRepository).findByIdWithAuthor(1L);
    }

    @Test
    @DisplayName("작성자는 게시글 수정에 성공한다")
    void updatePost_success() throws Exception {
        // given
        Post post = Post.builder()
                .author(author)
                .title("원래 제목")
                .content("원래 내용")
                .category(PostCategory.FREE)
                .viewCount(10)
                .build();
        setPostId(post, 1L);
        setCreatedAt(post, LocalDateTime.now());

        UpdatePostRequest request = UpdatePostRequest.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .build();

        given(postRepository.findByIdWithAuthor(1L)).willReturn(Optional.of(post));

        // when
        PostResponse response = postService.updatePost(author.getId(), 1L, request);

        // then
        assertThat(response.getTitle()).isEqualTo("수정된 제목");
        assertThat(response.getContent()).isEqualTo("수정된 내용");
        assertThat(response.getCategory()).isEqualTo(PostCategory.FREE); // category unchanged
        assertThat(response.getAuthorId()).isEqualTo(author.getId());

        verify(postRepository).findByIdWithAuthor(1L);
    }

    @Test
    @DisplayName("작성자가 아닌 사용자는 게시글을 수정할 수 없다")
    void updatePost_notAuthor() throws Exception {
        // given
        Post post = Post.builder()
                .author(author)
                .title("원래 제목")
                .content("원래 내용")
                .category(PostCategory.FREE)
                .viewCount(0)
                .build();
        setPostId(post, 1L);

        UpdatePostRequest request = UpdatePostRequest.builder()
                .title("수정 시도")
                .content("수정 시도")
                .build();

        given(postRepository.findByIdWithAuthor(1L)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.updatePost(anotherUser.getId(), 1L, request))
                .isInstanceOf(CustomException.class)
                .hasMessage("게시글 접근 권한이 없습니다.");

        verify(postRepository).findByIdWithAuthor(1L);
    }

    @Test
    @DisplayName("존재하지 않는 게시글은 수정할 수 없다")
    void updatePost_postNotFound() {
        // given
        Long nonExistentPostId = 999L;
        UpdatePostRequest request = UpdatePostRequest.builder()
                .title("수정 시도")
                .content("수정 시도")
                .build();

        given(postRepository.findByIdWithAuthor(nonExistentPostId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.updatePost(author.getId(), nonExistentPostId, request))
                .isInstanceOf(CustomException.class)
                .hasMessage("게시글을 찾을 수 없습니다.");

        verify(postRepository).findByIdWithAuthor(nonExistentPostId);
    }

    @Test
    @DisplayName("작성자는 게시글 삭제에 성공한다")
    void deletePost_success() throws Exception {
        // given
        Post post = Post.builder()
                .author(author)
                .title("삭제할 게시글")
                .content("삭제할 내용")
                .category(PostCategory.FREE)
                .viewCount(0)
                .build();
        setPostId(post, 1L);

        given(postRepository.findByIdWithAuthor(1L)).willReturn(Optional.of(post));

        // when
        postService.deletePost(author.getId(), 1L);

        // then
        assertThat(post.getDeletedAt()).isNotNull();
        verify(postRepository).findByIdWithAuthor(1L);
    }

    @Test
    @DisplayName("작성자가 아닌 사용자는 게시글을 삭제할 수 없다")
    void deletePost_notAuthor() throws Exception {
        // given
        Post post = Post.builder()
                .author(author)
                .title("삭제 시도")
                .content("삭제 시도")
                .category(PostCategory.FREE)
                .viewCount(0)
                .build();
        setPostId(post, 1L);

        given(postRepository.findByIdWithAuthor(1L)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.deletePost(anotherUser.getId(), 1L))
                .isInstanceOf(CustomException.class)
                .hasMessage("게시글 접근 권한이 없습니다.");

        assertThat(post.getDeletedAt()).isNull();
        verify(postRepository).findByIdWithAuthor(1L);
    }

    @Test
    @DisplayName("존재하지 않는 게시글은 삭제할 수 없다")
    void deletePost_postNotFound() {
        // given
        Long nonExistentPostId = 999L;
        given(postRepository.findByIdWithAuthor(nonExistentPostId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.deletePost(author.getId(), nonExistentPostId))
                .isInstanceOf(CustomException.class)
                .hasMessage("게시글을 찾을 수 없습니다.");

        verify(postRepository).findByIdWithAuthor(nonExistentPostId);
    }

    @Test
    @DisplayName("이미 삭제된 게시글은 다시 삭제할 수 없다")
    void deletePost_alreadyDeleted() throws Exception {
        // given
        Post deletedPost = Post.builder()
                .author(author)
                .title("이미 삭제된 게시글")
                .content("이미 삭제된 내용")
                .category(PostCategory.FREE)
                .viewCount(0)
                .build();
        setPostId(deletedPost, 1L);
        
        Field deletedAtField = Post.class.getSuperclass().getDeclaredField("deletedAt");
        deletedAtField.setAccessible(true);
        deletedAtField.set(deletedPost, LocalDateTime.now());

        given(postRepository.findByIdWithAuthor(1L)).willReturn(Optional.of(deletedPost));

        // when & then
        assertThatThrownBy(() -> postService.deletePost(author.getId(), 1L))
                .isInstanceOf(CustomException.class)
                .hasMessage("게시글을 찾을 수 없습니다.");

        verify(postRepository).findByIdWithAuthor(1L);
    }
}
