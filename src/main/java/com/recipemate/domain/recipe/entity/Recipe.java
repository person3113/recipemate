package com.recipemate.domain.recipe.entity;

import com.recipemate.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 레시피 엔티티
 * TheMealDB, 식품안전나라, 사용자 등록 레시피를 통합 관리
 */
@Entity
@Table(name = "recipes", indexes = {
        @Index(name = "idx_title", columnList = "title"),
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_area", columnList = "area"),
        @Index(name = "idx_calories", columnList = "calories"),
        @Index(name = "idx_source_api", columnList = "source_api")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_source_api_id", columnNames = {"source_api", "source_api_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE recipes SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Recipe extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 레시피명
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 대표 이미지 (대)
     */
    @Column(length = 500)
    private String fullImageUrl;

    /**
     * 대표 이미지 (소)
     */
    @Column(length = 500)
    private String thumbnailImageUrl;

    /**
     * 카테고리
     */
    @Column(length = 100)
    private String category;

    /**
     * 국가/지역
     */
    @Column(length = 100)
    private String area;

    /**
     * 데이터 출처
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "source_api")
    private RecipeSource sourceApi;

    /**
     * 외부 API의 고유 ID (idMeal, RCP_SEQ 등)
     */
    @Column(length = 100, name = "source_api_id")
    private String sourceApiId;

    /**
     * 레시피 작성자 (사용자 직접 등록 레시피만 해당, API 레시피는 null)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private com.recipemate.domain.user.entity.User author;

    /**
     * 조리 방법 (TheMealDB 형식 - 텍스트)
     */
    @Column(columnDefinition = "TEXT")
    private String instructions;

    /**
     * 열량 (kcal)
     */
    @Column
    private Integer calories;

    /**
     * 탄수화물 (g)
     */
    @Column
    private Integer carbohydrate;

    /**
     * 단백질 (g)
     */
    @Column
    private Integer protein;

    /**
     * 지방 (g)
     */
    @Column
    private Integer fat;

    /**
     * 나트륨 (mg)
     */
    @Column
    private Integer sodium;

    /**
     * 1회 제공량
     */
    @Column(length = 50)
    private String servingSize;

    /**
     * 저감 조리법 팁
     */
    @Column(length = 1000)
    private String tips;

    /**
     * YouTube 영상 URL (TheMealDB)
     */
    @Column(length = 500)
    private String youtubeUrl;

    /**
     * 원본 레시피 URL (TheMealDB: strSource)
     */
    @Column(length = 500)
    private String sourceUrl;

    /**
     * 마지막 동기화 시간
     */
    @Column(nullable = false)
    private LocalDateTime lastSyncedAt;

    /**
     * 레시피 재료 목록
     */
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    /**
     * 조리 단계 목록
     */
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RecipeStep> steps = new ArrayList<>();

    /**
     * 재료 추가
     */
    public void addIngredient(RecipeIngredient ingredient) {
        ingredients.add(ingredient);
        ingredient.setRecipe(this);
    }

    /**
     * 조리 단계 추가
     */
    public void addStep(RecipeStep step) {
        steps.add(step);
        step.setRecipe(this);
    }

    /**
     * 동기화 시간 업데이트
     */
    public void updateSyncTime() {
        this.lastSyncedAt = LocalDateTime.now();
    }

    /**
     * 사용자가 작성한 레시피인지 확인
     */
    public boolean isUserRecipe() {
        return this.sourceApi == RecipeSource.USER;
    }

    /**
     * 특정 사용자가 이 레시피를 수정할 수 있는지 확인
     * - 관리자(ADMIN)는 모든 레시피 수정 가능
     * - 일반 사용자는 본인이 작성한 레시피만 수정 가능
     */
    public boolean canModify(com.recipemate.domain.user.entity.User user) {
        if (user == null) {
            return false;
        }
        
        // 관리자는 모든 레시피 수정 가능
        if (user.getRole() == com.recipemate.global.common.UserRole.ADMIN) {
            return true;
        }
        
        // 일반 사용자는 본인이 작성한 사용자 레시피만 수정 가능
        return isUserRecipe() && author != null && author.getId().equals(user.getId());
    }

    /**
     * 대표 이미지 업데이트
     */
    public void updateMainImage(String imageUrl) {
        this.fullImageUrl = imageUrl;
        this.thumbnailImageUrl = imageUrl;
    }

    /**
     * 기본 정보 업데이트
     */
    public void updateBasicInfo(String title, String category, String area,
                                 String tips, String youtubeUrl, String sourceUrl) {
        if (title != null) {
            this.title = title;
        }
        if (category != null) {
            this.category = category;
        }
        if (area != null) {
            this.area = area;
        }
        if (tips != null) {
            this.tips = tips;
        }
        if (youtubeUrl != null) {
            this.youtubeUrl = youtubeUrl;
        }
        if (sourceUrl != null) {
            this.sourceUrl = sourceUrl;
        }
        this.lastSyncedAt = LocalDateTime.now();
    }
}
