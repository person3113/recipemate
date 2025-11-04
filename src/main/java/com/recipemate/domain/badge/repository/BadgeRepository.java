package com.recipemate.domain.badge.repository;

import com.recipemate.domain.badge.entity.Badge;
import com.recipemate.global.common.BadgeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    List<Badge> findByUserIdOrderByAcquiredAtDesc(Long userId);

    boolean existsByUserIdAndBadgeType(Long userId, BadgeType badgeType);
}
