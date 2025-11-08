package com.recipemate.domain.groupbuy.repository;

import com.recipemate.domain.groupbuy.entity.GroupBuy;
import com.recipemate.global.common.GroupBuyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GroupBuyRepository extends JpaRepository<GroupBuy, Long>, JpaSpecificationExecutor<GroupBuy>, GroupBuyRepositoryCustom {

    Page<GroupBuy> findByStatusOrderByDeadlineAsc(GroupBuyStatus status, Pageable pageable);

    Page<GroupBuy> findByStatus(GroupBuyStatus status, Pageable pageable);

    Page<GroupBuy> findByHostId(Long hostId, Pageable pageable);
    
    @Query("SELECT g FROM GroupBuy g WHERE g.host.id = :hostId AND g.deletedAt IS NULL ORDER BY g.createdAt DESC")
    Page<GroupBuy> findByHostIdAndNotDeleted(@Param("hostId") Long hostId, Pageable pageable);

    List<GroupBuy> findByRecipeApiId(String recipeApiId);
    
    @Query("SELECT g FROM GroupBuy g WHERE g.recipeApiId = :recipeApiId AND g.deletedAt IS NULL")
    List<GroupBuy> findByRecipeApiIdAndNotDeleted(@Param("recipeApiId") String recipeApiId);

    @Query("SELECT g FROM GroupBuy g WHERE g.category = :category AND g.status = :status ORDER BY g.createdAt DESC")
    Page<GroupBuy> findByCategoryAndStatus(@Param("category") com.recipemate.global.common.GroupBuyCategory category, 
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
    
    @Query("SELECT g FROM GroupBuy g WHERE g.host.id = :hostId AND g.status IN :statuses AND g.deletedAt IS NULL ORDER BY g.createdAt DESC")
    Page<GroupBuy> findByHostIdAndStatusInAndNotDeleted(@Param("hostId") Long hostId, 
                                                         @Param("statuses") List<GroupBuyStatus> statuses, 
                                                         Pageable pageable);

    long countByHostIdAndStatus(Long hostId, GroupBuyStatus status);

    boolean existsByHostIdAndRecipeApiIdAndStatus(Long hostId, String recipeApiId, GroupBuyStatus status);

    @Query("SELECT DISTINCT g FROM GroupBuy g LEFT JOIN FETCH g.host WHERE g.id = :id")
    Optional<GroupBuy> findByIdWithHost(@Param("id") Long id);

    @Query("SELECT DISTINCT g FROM GroupBuy g LEFT JOIN FETCH g.images WHERE g.id = :id")
    Optional<GroupBuy> findByIdWithImages(@Param("id") Long id);

    @Query("SELECT g FROM GroupBuy g LEFT JOIN FETCH g.host WHERE g.id = :id")
    Optional<GroupBuy> findByIdWithHostAndImages(@Param("id") Long id);

    // 배치 작업용 쿼리 메서드
    @Query("SELECT g FROM GroupBuy g WHERE g.status IN :statuses AND g.deadline < :now")
    List<GroupBuy> findByStatusInAndDeadlineBefore(@Param("statuses") List<GroupBuyStatus> statuses,
                                                    @Param("now") LocalDateTime now);

    @Query("SELECT g FROM GroupBuy g WHERE g.status = :status AND g.deadline BETWEEN :start AND :end")
    List<GroupBuy> findByStatusAndDeadlineBetween(@Param("status") GroupBuyStatus status,
                                                   @Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);
}
