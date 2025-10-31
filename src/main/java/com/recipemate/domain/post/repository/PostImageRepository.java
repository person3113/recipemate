package com.recipemate.domain.post.repository;

import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.post.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    List<PostImage> findByPostIdOrderByDisplayOrderAsc(Long postId);
    
    List<PostImage> findByPostOrderByDisplayOrderAsc(Post post);
}
