package com.recipemate.domain.groupbuy.repository;

import com.recipemate.domain.groupbuy.entity.Participation;
import com.recipemate.global.common.GroupBuyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    /**
     * 특정 사용자와 공구의 참여 정보 조회
     * 중복 참여 체크 시 사용
     */
    Optional<Participation> findByUserIdAndGroupBuyId(Long userId, Long groupBuyId);

    @Query("SELECT p FROM Participation p JOIN FETCH p.groupBuy WHERE p.user.id = :userId AND p.groupBuy.id = :groupBuyId")
    Optional<Participation> findByUserIdAndGroupBuyIdWithGroupBuy(@Param("userId") Long userId, @Param("groupBuyId") Long groupBuyId);


    /**
     * 특정 공구의 모든 참여자 조회
     */
    List<Participation> findByGroupBuyId(Long groupBuyId);

    /**
     * 특정 공구의 참여자 수 조회
     */
    long countByGroupBuyId(Long groupBuyId);

    /**
     * 특정 사용자가 참여한 모든 공구 조회
     */
    @Query("SELECT p FROM Participation p JOIN FETCH p.groupBuy WHERE p.user.id = :userId ORDER BY p.participatedAt DESC")
    List<Participation> findByUserIdWithGroupBuy(@Param("userId") Long userId);

    /**
     * 특정 공구의 참여자 목록 (사용자 정보 포함)
     */
    @Query("SELECT p FROM Participation p JOIN FETCH p.user WHERE p.groupBuy.id = :groupBuyId ORDER BY p.participatedAt ASC")
    List<Participation> findByGroupBuyIdWithUser(@Param("groupBuyId") Long groupBuyId);

    /**
     * 특정 공구의 총 주문 수량 계산
     */
    @Query("SELECT COALESCE(SUM(p.quantity), 0) FROM Participation p WHERE p.groupBuy.id = :groupBuyId")
    int sumQuantityByGroupBuyId(@Param("groupBuyId") Long groupBuyId);

    /**
     * 특정 사용자가 특정 공구에 참여했는지 확인
     */
    boolean existsByUserIdAndGroupBuyId(Long userId, Long groupBuyId);

    /**
     * 특정 사용자가 참여한 공구 수
     */
    long countByUserId(Long userId);

    /**
     * 특정 사용자가 참여한 공구 목록 조회 (페이징, 상태별 필터링)
     */
    @Query("SELECT p FROM Participation p " +
           "JOIN FETCH p.groupBuy gb " +
           "JOIN FETCH gb.host " +
           "WHERE p.user.id = :userId " +
           "AND (:statuses IS NULL OR gb.status IN :statuses) " +
           "ORDER BY p.participatedAt DESC")
    Page<Participation> findByUserIdWithGroupBuyAndHost(
            @Param("userId") Long userId,
            @Param("statuses") List<GroupBuyStatus> statuses,
            Pageable pageable);
}
