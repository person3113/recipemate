package com.recipemate.domain.recipe.entity;

import com.recipemate.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 조리 단계 엔티티
 * 레시피의 각 조리 단계를 순서대로 관리
 */
@Entity
@Table(name = "recipe_steps", indexes = {
        @Index(name = "idx_recipe_id_step_number", columnList = "recipe_id, step_number")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE recipe_steps SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class RecipeStep extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연결된 레시피
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    @Setter
    private Recipe recipe;

    /**
     * 단계 번호 (1, 2, 3...)
     */
    @Column(nullable = false)
    private Integer stepNumber;

    /**
     * 단계별 설명
     * 예: strInstructions 파싱, MANUALXX
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * 단계별 이미지
     * 예: MANUAL_IMGXX
     */
    @Column(length = 500)
    private String imageUrl;
}
