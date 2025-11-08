package com.recipemate.domain.user.repository;

import com.recipemate.domain.user.entity.PointHistory;
import com.recipemate.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    Page<PointHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    boolean existsByUserAndDescriptionAndCreatedAtBetween(
            User user,
            String description,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );
}
