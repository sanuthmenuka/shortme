package com.sanuth.shortme.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class CreateShortLinkRequest {

    @NotBlank(message = "Long URL is required")
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    private String longUrl;

    @Size(min = 3, max = 20, message = "Custom short code must be between 3 and 20 characters if provided")
    @Pattern(regexp = "^[a-zA-Z0-9-_]+$", message = "Short code can only contain alphanumeric characters, hyphens, and underscores")
    private String customShortCode; // Optional

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant expiresAt; // Optional

    public CreateShortLinkRequest() {
    }

    public CreateShortLinkRequest(String longUrl, String customShortCode, Instant expiresAt) {
        this.longUrl = longUrl;
        this.customShortCode = customShortCode;
        this.expiresAt = expiresAt;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public String getCustomShortCode() {
        return customShortCode;
    }

    public void setCustomShortCode(String customShortCode) {
        this.customShortCode = customShortCode;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
