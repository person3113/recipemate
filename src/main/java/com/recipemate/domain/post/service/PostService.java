package com.recipemate.domain.post.service;

import com.recipemate.domain.comment.repository.CommentRepository;
import com.recipemate.domain.like.repository.PostLikeRepository;
import com.recipemate.domain.post.dto.CreatePostRequest;
import com.recipemate.domain.post.dto.PostResponse;
import com.recipemate.domain.post.dto.PostWithCountsDto;
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
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;

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

    public PostResponse getPostDetail(Long postId) {
        Post post = postRepository.findByIdWithAuthor(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (post.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        
        PostResponse response = PostResponse.from(post);
        return enrichWithCountsAndLikeInfo(response, post, null);
    }

    public PostResponse getPostDetail(Long postId, Long currentUserId) {
        Post post = postRepository.findByIdWithAuthor(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (post.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        
        PostResponse response = PostResponse.from(post);
        return enrichWithCountsAndLikeInfo(response, post, currentUserId);
    }

    /**
     * 게시글 조회수 증가
     * CQS 원칙에 따라 조회(Query)와 상태 변경(Command)을 분리
     * @param postId 게시글 ID
     */
    @Transactional
    public void increasePostViewCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        
        if (post.getDeletedAt() != null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        
        post.increaseViewCount();
    }

    private PostResponse enrichWithCountsAndLikeInfo(PostResponse response, Post post, Long currentUserId) {
        long likeCount = postLikeRepository.countByPost(post);
        long commentCount = commentRepository.countByPostId(post.getId());
        boolean isLiked = false;
        
        if (currentUserId != null) {
            User user = userRepository.findById(currentUserId).orElse(null);
            if (user != null) {
                isLiked = postLikeRepository.existsByUserAndPost(user, post);
            }
        }
        
        return PostResponse.builder()
                .id(response.getId())
                .title(response.getTitle())
                .content(response.getContent())
                .category(response.getCategory())
                .viewCount(response.getViewCount())
                .authorId(response.getAuthorId())
                .authorNickname(response.getAuthorNickname())
                .authorEmail(response.getAuthorEmail())
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .isLiked(isLiked)
                .build();
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

    /**
     * 게시글 목록 조회 (필터링 + 검색 + 페이징 + 동적 정렬)
     * QueryDSL 기반 동적 쿼리로 통합하여 likeCount, commentCount 정렬 지원
     * 캐싱 적용으로 반복 조회 시 성능 향상
     * @param category 카테고리 필터 (선택)
     * @param keyword 검색 키워드 (선택)
     * @param pageable 페이징 및 정렬 정보
     * @return 게시글 목록 (페이징)
     */
    @Cacheable(
            value = CacheConfig.VIEW_COUNTS_CACHE,
            key = "'post_list:' + (#category != null ? #category.name() : 'all') + ':' + (#keyword != null ? #keyword : 'none') + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()",
            unless = "#result.isEmpty()"
    )
    public Page<PostResponse> getPostList(PostCategory category, String keyword, Pageable pageable) {
        Page<PostWithCountsDto> postsWithCounts;
        String trimmedKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        if (category != null && trimmedKeyword != null) {
            // 카테고리 + 키워드(댓글 포함) 검색
            postsWithCounts = postRepository.searchByCategoryAndKeywordWithCounts(category, trimmedKeyword, pageable);
        } else if (category != null) {
            // 카테고리만 필터링
            postsWithCounts = postRepository.findByCategoryWithCounts(category, pageable);
        } else if (trimmedKeyword != null) {
            // 키워드만 검색(댓글 포함)
            postsWithCounts = postRepository.searchByKeywordWithCounts(trimmedKeyword, pageable);
        } else {
            // 전체 목록 조회
            postsWithCounts = postRepository.findAllWithCounts(pageable);
        }

        // DTO 변환 (from develop branch)
        return postsWithCounts.map(dto -> {
            Post post = dto.getPost();
            return PostResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .category(post.getCategory())
                    .viewCount(post.getViewCount())
                    .authorId(post.getAuthor().getId())
                    .authorNickname(post.getAuthor().getNickname())
                    .authorEmail(post.getAuthor().getEmail())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .likeCount(dto.getLikeCount())
                    .commentCount(dto.getCommentCount())
                    .isLiked(false) // 목록에서는 '좋아요' 여부 확인 불가
                    .build();
        });
    }
}
