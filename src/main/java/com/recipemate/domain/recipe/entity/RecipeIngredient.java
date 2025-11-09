package com.recipemate.domain.recipe.entity;

import com.recipemate.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 레시피 재료 엔티티
 * 레시피에 포함되는 재료와 계량 정보를 관리
 */
@Entity
@Table(name = "recipe_ingredients", indexes = {
        @Index(name = "idx_recipe_ingredient_recipe_id", columnList = "recipe_id"),
        @Index(name = "idx_recipe_ingredient_name", columnList = "name")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class RecipeIngredient extends BaseEntity {

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
     * 재료명 (API 원본 재료명)
     * MVP: API에서 받은 재료명을 그대로 저장
     * 예: strIngredientN, RCP_PARTS_DTLS 파싱 결과
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * 계량 정보
     * 예: "1 cup", "200g", "적당량"
     */
    @Column(nullable = false, length = 200)
    private String measure;
}
