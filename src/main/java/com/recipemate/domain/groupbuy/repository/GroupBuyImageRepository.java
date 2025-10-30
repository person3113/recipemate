package com.recipemate.domain.groupbuy.repository;

import com.recipemate.domain.groupbuy.entity.GroupBuyImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupBuyImageRepository extends JpaRepository<GroupBuyImage, Long> {

    List<GroupBuyImage> findByGroupBuyIdOrderByDisplayOrderAsc(Long groupBuyId);
}
