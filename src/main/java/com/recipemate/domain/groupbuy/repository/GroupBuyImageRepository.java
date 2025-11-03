package com.recipemate.domain.groupbuy.repository;

import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.domain.groupbuy.entity.GroupBuyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupBuyImageRepository extends JpaRepository<GroupBuyImage, Long> {

    List<GroupBuyImage> findByGroupBuyIdOrderByDisplayOrderAsc(Long groupBuyId);
    
    List<GroupBuyImage> findByGroupBuyOrderByDisplayOrderAsc(GroupBuy groupBuy);
    
    /**
     * 여러 공구의 이미지를 한 번에 조회 (N+1 문제 해결)
     * @param groupBuyIds 공구 ID 목록
     * @return 이미지 목록 (displayOrder로 정렬)
     */
    @Query("SELECT i FROM GroupBuyImage i WHERE i.groupBuy.id IN :groupBuyIds ORDER BY i.groupBuy.id, i.displayOrder")
    List<GroupBuyImage> findByGroupBuyIdInOrderByGroupBuyIdAndDisplayOrder(@Param("groupBuyIds") List<Long> groupBuyIds);
}
