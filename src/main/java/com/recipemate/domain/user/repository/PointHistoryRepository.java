package com.recipemate.domain.user.repository;

import com.recipemate.domain.user.entity.PointHistory;
import com.recipemate.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    Page<PointHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
