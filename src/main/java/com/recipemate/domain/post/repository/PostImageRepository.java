package com.recipemate.domain.post.repository;

import com.recipemate.domain.post.entity.Post;
import com.recipemate.domain.post.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    List<PostImage> findByPostIdOrderByDisplayOrderAsc(Long postId);
    
    List<PostImage> findByPostOrderByDisplayOrderAsc(Post post);
    
    /**
     * 여러 게시글의 이미지를 한 번에 조회 (N+1 문제 해결)
     * @param postIds 게시글 ID 목록
     * @return 이미지 목록 (displayOrder로 정렬)
     */
    @Query("SELECT i FROM PostImage i WHERE i.post.id IN :postIds ORDER BY i.post.id, i.displayOrder")
    List<PostImage> findByPostIdInOrderByPostIdAndDisplayOrder(@Param("postIds") List<Long> postIds);
}

