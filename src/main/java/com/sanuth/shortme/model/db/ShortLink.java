package com.sanuth.shortme.model.db;

import com.sanuth.shortme.model.dto.CreateShortLinkRequest;
import jakarta.persistence.*;

import java.time.Instant;


@Entity
public class ShortLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String shortCode;
    private String longUrl;
    private Instant createdAt;
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    private  LinkStatus status;

    // Static factory method to create ShortLink from request
    public static ShortLink create(CreateShortLinkRequest request) {
        ShortLink shortLink = new ShortLink();
        shortLink.setLongUrl(request.getLongUrl());
        shortLink.setShortCode(request.getCustomShortCode());
        shortLink.setExpiresAt(request.getExpiresAt());
        shortLink.setCreatedAt(Instant.now());
        shortLink.setStatus(LinkStatus.ACTIVE);
        return shortLink;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
}
