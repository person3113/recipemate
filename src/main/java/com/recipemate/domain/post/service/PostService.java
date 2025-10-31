package com.recipemate.domain.post.service;

import com.recipemate.domain.post.dto.CreatePostRequest;
import com.recipemate.domain.post.dto.PostResponse;
import com.recipemate.domain.post.dto.UpdatePostRequest;
import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.post.repository.PostRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.PostCategory;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public PostResponse createPost(Long userId, CreatePostRequest request) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = Post.builder()
                .author(author)
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .viewCount(0)
                .build();

        Post savedPost = postRepository.save(post);
        return PostResponse.from(savedPost);
    }

    @Transactional
    public PostResponse getPostDetail(Long postId) {
        Post post = postRepository.findByIdWithAuthor(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (post.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }

        post.increaseViewCount();
        return PostResponse.from(post);
    }

    @Transactional
    public PostResponse updatePost(Long userId, Long postId, UpdatePostRequest request) {
        Post post = postRepository.findByIdWithAuthor(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (post.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }

        if (!post.getAuthor().getId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_POST_ACCESS);
        }

        post.update(request.getTitle(), request.getContent());
        return PostResponse.from(post);
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = postRepository.findByIdWithAuthor(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (post.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }

        if (!post.getAuthor().getId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_POST_ACCESS);
        }

        post.delete();
    }

    /**
     * 게시글 목록 조회 (필터링 + 검색 + 페이징)
     * @param category 카테고리 필터 (선택)
     * @param keyword 검색 키워드 (선택)
     * @param pageable 페이징 정보
     * @return 게시글 목록 (페이징)
     */
    public Page<PostResponse> getPostList(PostCategory category, String keyword, Pageable pageable) {
        Page<Post> posts;

        // 조건에 따라 적절한 repository 메서드 호출
        if (category != null && keyword != null && !keyword.trim().isEmpty()) {
            // 카테고리 + 키워드 검색
            posts = postRepository.searchByCategoryAndKeyword(category, keyword.trim(), pageable);
        } else if (category != null) {
            // 카테고리만 필터링
            posts = postRepository.findByCategoryAndDeletedAtIsNull(category, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            // 키워드만 검색
            posts = postRepository.searchByKeyword(keyword.trim(), pageable);
        } else {
            // 전체 목록 조회
            posts = postRepository.findAllByDeletedAtIsNull(pageable);
        }

        return posts.map(PostResponse::from);
    }
}
