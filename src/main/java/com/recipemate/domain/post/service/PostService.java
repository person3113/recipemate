// java
package com.recipemate.domain.post.service;

import com.recipemate.domain.post.dto.CreatePostRequest;
import com.recipemate.domain.post.dto.PostResponse;
import com.recipemate.domain.post.dto.UpdatePostRequest;
import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.post.repository.PostRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.common.PostCategory;
import com.recipemate.global.config.CacheConfig;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    @CacheEvict(value = CacheConfig.VIEW_COUNTS_CACHE, allEntries = true)
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
        log.debug("Post created and cache evicted");
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
    @CacheEvict(value = CacheConfig.VIEW_COUNTS_CACHE, allEntries = true)
    public PostResponse updatePost(Long userId, Long postId, UpdatePostRequest request) {
        Post post = postRepository.findByIdWithAuthor(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (post.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }

        if (!post.getAuthor().getId().equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_POST_ACCESS);
        }

        post.update(request.getTitle(), request.getContent(), request.getCategory());
        log.debug("Post updated and cache evicted");
        return PostResponse.from(post);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.VIEW_COUNTS_CACHE, allEntries = true)
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
        log.debug("Post deleted and cache evicted");
    }

    @Cacheable(
            value = CacheConfig.VIEW_COUNTS_CACHE,
            key = "'post_list:' + (#category != null ? #category.name() : 'all') + ':' + (#keyword != null ? #keyword : 'none') + ':' + #pageable.pageNumber + ':' + #pageable.pageSize",
            unless = "#result.isEmpty()"
    )
    public Page<PostResponse> getPostList(PostCategory category, String keyword, Pageable pageable) {
        Page<Post> posts;
        String trimmedKeyword = (keyword != null ? keyword.trim() : null);

        if (category != null && trimmedKeyword != null && !trimmedKeyword.isEmpty()) {
            // 카테고리 + 키워드(댓글 포함) 검색
            posts = postRepository.searchByCategoryAndKeyword(category, trimmedKeyword, pageable);
        } else if (category != null) {
            // 카테고리만 필터링
            posts = postRepository.findByCategoryAndDeletedAtIsNull(category, pageable);
        } else if (trimmedKeyword != null && !trimmedKeyword.isEmpty()) {
            // 키워드만 검색(댓글 포함)
            posts = postRepository.searchByKeywordIncludingComments(trimmedKeyword, pageable);
        } else {
            // 전체 목록 조회
            posts = postRepository.findAllByDeletedAtIsNull(pageable);
        }

        return posts.map(PostResponse::from);
    }
}
