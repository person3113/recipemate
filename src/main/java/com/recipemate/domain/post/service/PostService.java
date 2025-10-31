package com.recipemate.domain.post.service;

import com.recipemate.domain.post.dto.CreatePostRequest;
import com.recipemate.domain.post.dto.PostResponse;
import com.recipemate.domain.post.dto.UpdatePostRequest;
import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.post.repository.PostRepository;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
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
}
