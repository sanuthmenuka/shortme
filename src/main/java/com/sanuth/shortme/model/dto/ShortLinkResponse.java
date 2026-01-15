package com.sanuth.shortme.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sanuth.shortme.model.db.LinkStatus;
import com.sanuth.shortme.model.db.ShortLink;

import java.time.Instant;

public class ShortLinkResponse {

    private String shortCode;
    private String shortUrl;
    private String longUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant expiresAt;

    private LinkStatus status;

    public ShortLinkResponse() {
    }

    public ShortLinkResponse(ShortLink shortLink) {
        this.shortCode = shortLink.getShortCode();
        this.longUrl = shortLink.getLongUrl();
        this.createdAt = shortLink.getCreatedAt();
        this.expiresAt = shortLink.getExpiresAt();
        this.status = shortLink.getStatus();
    }

    public ShortLinkResponse(ShortLink shortLink, String baseUrl) {
        this(shortLink);
        this.shortUrl = baseUrl + "/" + shortLink.getShortCode();
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
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