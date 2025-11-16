package com.recipemate.domain.groupbuy.entity;

import com.recipemate.global.common.GroupBuyStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
public class GroupPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private GroupBuyStatus status;

    private LocalDate deadline;

    private boolean deletable;
}
