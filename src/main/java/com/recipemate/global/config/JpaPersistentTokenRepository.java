package com.recipemate.global.config;

import com.recipemate.domain.user.entity.PersistentToken;
import com.recipemate.domain.user.repository.PersistentTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * JPA 기반 PersistentTokenRepository 구현체
 * Spring Security의 Remember-Me 기능과 연동
 */
@Component
@RequiredArgsConstructor
public class JpaPersistentTokenRepository implements PersistentTokenRepository {

    private final PersistentTokenJpaRepository tokenRepository;

    @Override
    @Transactional
    public void createNewToken(PersistentRememberMeToken token) {
        PersistentToken persistentToken = new PersistentToken(
                token.getSeries(),
                token.getUsername(),
                token.getTokenValue(),
                convertToLocalDateTime(token.getDate())
        );
        tokenRepository.save(persistentToken);
    }

    @Override
    @Transactional
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        tokenRepository.findBySeries(series).ifPresent(token -> {
            token.updateToken(tokenValue, convertToLocalDateTime(lastUsed));
            tokenRepository.save(token);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        return tokenRepository.findBySeries(seriesId)
                .map(token -> new PersistentRememberMeToken(
                        token.getUsername(),
                        token.getSeries(),
                        token.getToken(),
                        convertToDate(token.getLastUsed())
                ))
                .orElse(null);
    }

    @Override
    @Transactional
    public void removeUserTokens(String username) {
        tokenRepository.deleteByUsername(username);
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    private Date convertToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }
}
