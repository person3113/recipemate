package com.recipemate.domain.groupbuy.repository;

import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.global.common.GroupBuyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GroupBuyRepository extends JpaRepository<GroupBuy, Long> {

    List<GroupBuy> findByStatusOrderByDeadlineAsc(GroupBuyStatus status);

    Page<GroupBuy> findByStatus(GroupBuyStatus status, Pageable pageable);

    Page<GroupBuy> findByHostId(Long hostId, Pageable pageable);

    List<GroupBuy> findByRecipeApiId(String recipeApiId);

    @Query("SELECT g FROM GroupBuy g WHERE g.category = :category AND g.status = :status ORDER BY g.createdAt DESC")
    Page<GroupBuy> findByCategoryAndStatus(@Param("category") String category, 
                                           @Param("status") GroupBuyStatus status, 
                                           Pageable pageable);

    @Query("SELECT g FROM GroupBuy g WHERE g.status = :status AND g.deadline BETWEEN :start AND :end ORDER BY g.deadline ASC")
    List<GroupBuy> findImminentGroupBuys(@Param("status") GroupBuyStatus status,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Query("SELECT g FROM GroupBuy g WHERE g.recipeApiId IS NOT NULL AND g.status = :status ORDER BY g.createdAt DESC")
    Page<GroupBuy> findRecipeBasedGroupBuys(@Param("status") GroupBuyStatus status, Pageable pageable);

    @Query("SELECT g FROM GroupBuy g WHERE (g.title LIKE %:keyword% OR g.content LIKE %:keyword%) AND g.status = :status ORDER BY g.createdAt DESC")
    Page<GroupBuy> searchByKeyword(@Param("keyword") String keyword, 
                                    @Param("status") GroupBuyStatus status, 
                                    Pageable pageable);

    @Query("SELECT g FROM GroupBuy g WHERE g.status = :status ORDER BY g.currentHeadcount DESC")
    Page<GroupBuy> findByStatusOrderByParticipantsDesc(@Param("status") GroupBuyStatus status, Pageable pageable);

    @Query("SELECT g FROM GroupBuy g WHERE g.host.id = :hostId AND g.status IN :statuses ORDER BY g.createdAt DESC")
    Page<GroupBuy> findByHostIdAndStatusIn(@Param("hostId") Long hostId, 
                                           @Param("statuses") List<GroupBuyStatus> statuses, 
                                           Pageable pageable);

    long countByHostIdAndStatus(Long hostId, GroupBuyStatus status);

    boolean existsByHostIdAndRecipeApiIdAndStatus(Long hostId, String recipeApiId, GroupBuyStatus status);
}
