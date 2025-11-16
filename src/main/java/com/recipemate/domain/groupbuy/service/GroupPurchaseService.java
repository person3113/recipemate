package com.recipemate.domain.groupbuy.service;

import com.recipemate.domain.groupbuy.entity.GroupPurchase;
import com.recipemate.domain.groupbuy.repository.GroupPurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupPurchaseService {

    private final GroupPurchaseRepository groupPurchaseRepository;

    public GroupPurchase findById(Long id) {
        return groupPurchaseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GroupPurchase not found"));
    }
}
