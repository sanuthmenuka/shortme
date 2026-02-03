package com.sanuth.shortme.model.cache;

import com.sanuth.shortme.model.db.LinkStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.time.Instant;

@RedisHash("shortlinks")
public class CachedShortLink {

    @Id
    private String shortCode;

    @Indexed
    private String longUrl;

    private Long id;
    private Instant createdAt;
    private Instant expiresAt;
    private LinkStatus status;

    @TimeToLive
    private Long ttl;

    public CachedShortLink() {
    }

    public CachedShortLink(Long id, String shortCode, String longUrl, Instant createdAt,
                          Instant expiresAt, LinkStatus status) {
        this.id = id;
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.status = status;
        this.ttl = 3600L; // 1 hour default TTL
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LinkStatus getStatus() {
        return status;
    }

    public void setStatus(LinkStatus status) {
        this.status = status;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }
}
