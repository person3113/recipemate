package com.recipemate.domain.user.service;

import com.recipemate.domain.user.dto.MannerTempHistoryResponse;
import com.recipemate.domain.user.entity.MannerTempHistory;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.MannerTempHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MannerTempHistoryService {

    private final MannerTempHistoryRepository mannerTempHistoryRepository;

    @Transactional
    public void recordHistory(
            User user, 
            Double changeValue, 
            Double previousTemperature,
            String reason, 
            Long relatedReviewId
    ) {
        Double newTemperature = previousTemperature + changeValue;
        
        MannerTempHistory history = MannerTempHistory.create(
                user, 
                changeValue, 
                previousTemperature,
                newTemperature,
                reason, 
                relatedReviewId
        );
        
        mannerTempHistoryRepository.save(history);
    }

    public Page<MannerTempHistory> getHistoriesByUser(Long userId, Pageable pageable) {
        return mannerTempHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    public Page<MannerTempHistoryResponse> getHistoriesWithGroupBuyId(Long userId, Pageable pageable) {
        Page<Object[]> results = mannerTempHistoryRepository.findByUserIdWithGroupBuyId(userId, pageable);
        return results.map(result -> {
            MannerTempHistory history = (MannerTempHistory) result[0];
            Long groupBuyId = (Long) result[1];
            return MannerTempHistoryResponse.from(history, groupBuyId);
        });
    }
}
