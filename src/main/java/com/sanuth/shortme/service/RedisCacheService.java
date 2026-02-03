package com.sanuth.shortme.service;

import com.sanuth.shortme.model.cache.CachedShortLink;
import com.sanuth.shortme.model.db.ShortLink;
import com.sanuth.shortme.repository.CachedShortLinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RedisCacheService {
    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);
    private final CachedShortLinkRepository cachedShortLinkRepository;

    public RedisCacheService(CachedShortLinkRepository cachedShortLinkRepository) {
        this.cachedShortLinkRepository = cachedShortLinkRepository;
    }

    public void cacheShortLink(ShortLink shortLink) {
        try {
            CachedShortLink cached = new CachedShortLink(
                    shortLink.getId(),
                    shortLink.getShortCode(),
                    shortLink.getLongUrl(),
                    shortLink.getCreatedAt(),
                    shortLink.getExpiresAt(),
                    shortLink.getStatus()
            );
            cachedShortLinkRepository.save(cached);
            log.debug("Cached short link with code: {}", shortLink.getShortCode());
        } catch (Exception e) {
            log.warn("Failed to cache short link with code: {} - {}", shortLink.getShortCode(), e.getMessage());
        }
    }

    public Optional<String> getLongUrlByShortCode(String shortCode) {
        try {
            Optional<CachedShortLink> cached = cachedShortLinkRepository.findById(shortCode);
            if (cached.isPresent()) {
                log.info("Cache hit for short code: {}", shortCode);
                return Optional.of(cached.get().getLongUrl());
            } else {
                log.debug("Cache miss for short code: {}", shortCode);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.warn("Redis cache lookup failed for code: {} - {}", shortCode, e.getMessage());
            return Optional.empty();
        }
    }

    public void invalidateCache(String shortCode) {
        try {
            cachedShortLinkRepository.deleteById(shortCode);
            log.debug("Invalidated cache for short code: {}", shortCode);
        } catch (Exception e) {
            log.warn("Failed to invalidate cache for code: {} - {}", shortCode, e.getMessage());
        }
    }

    public boolean isCacheAvailable() {
        try {
            cachedShortLinkRepository.count();
            return true;
        } catch (Exception e) {
            log.warn("Redis cache is not available: {}", e.getMessage());
            return false;
        }
    }
}
