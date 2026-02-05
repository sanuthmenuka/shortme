package com.sanuth.shortme.service;

import com.sanuth.shortme.model.db.ShortLink;
import com.sanuth.shortme.model.dto.CreateShortLinkRequest;
import com.sanuth.shortme.model.dto.ShortLinkResponse;
import com.sanuth.shortme.repository.LinkRepository;
import com.sanuth.shortme.util.Base62Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Service
public class LinkService {
    private static final Logger log = LoggerFactory.getLogger(LinkService.class);
    private final LinkRepository linkRepository;
    private final RedisCacheService redisCacheService;

    public LinkService(LinkRepository linkRepository, RedisCacheService redisCacheService) {
        this.linkRepository = linkRepository;
        this.redisCacheService = redisCacheService;
    }

    public ShortLinkResponse createShortLink(CreateShortLinkRequest request) {
        log.info("Creating short link for URL: {}", request.getLongUrl());

        // Validate URL - let exceptions propagate to GlobalExceptionHandler
        validateUrl(request);

        ShortLink shortLink = ShortLink.create(request);

        // Use custom short code if provided, otherwise generate after saving
        if (request.getCustomShortCode() != null && !request.getCustomShortCode().trim().isEmpty()) {
            log.info("Using custom short code: {}", request.getCustomShortCode());

            // Check if custom short code already exists
            if (linkRepository.findByShortCode(request.getCustomShortCode()).isPresent()) {
                log.warn("Custom short code already exists: {}", request.getCustomShortCode());
                throw new IllegalArgumentException("Short code '" + request.getCustomShortCode() + "' is already in use");
            }

        } else {
            // Save first to get the auto-generated ID
            shortLink = linkRepository.save(shortLink);
            // Generate short code from ID using Base62
            String generatedCode = Base62Encoder.encode(shortLink.getId());
            log.info("Generated short code: {} from ID: {}", generatedCode, shortLink.getId());
            shortLink.setShortCode(generatedCode);
            // Update with the generated short code
        }
        shortLink=linkRepository.save(shortLink);

        log.info("Successfully created short link with code: {}", shortLink.getShortCode());

        // Cache the short link in Redis
        redisCacheService.cacheShortLink(shortLink);

        return new ShortLinkResponse(shortLink);
    }

    private void validateUrl(CreateShortLinkRequest request) {
        String url = request.getLongUrl();
        log.debug("Validating URL: {}", url);

        if (url == null || url.trim().isEmpty()) {
            log.warn("Validation failed: URL is empty");
            throw new IllegalArgumentException("URL cannot be empty");
        }

        // Check length (typical max: 2048 chars)
        if (url.length() > 2048) {
            log.warn("Validation failed: URL too long ({} chars)", url.length());
            throw new IllegalArgumentException("URL too long (max 2048 characters)");
        }

        // Add protocol if missing
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
            log.debug("Added https:// prefix to URL: {}", url);
        }

        // Reject double-prefixed URLs (e.g. "https://https://example.com")
        String withoutScheme = url.startsWith("https://") ? url.substring(8) : url.substring(7);
        if (withoutScheme.startsWith("http://") || withoutScheme.startsWith("https://")) {
            log.warn("Validation failed: double protocol prefix detected");
            throw new IllegalArgumentException("Invalid URL: double protocol prefix");
        }

        // Reject URLs with no valid host (must contain a dot, e.g. "example.com")
        String host = withoutScheme.split("/")[0];
        if (!host.contains(".")) {
            log.warn("Validation failed: no valid domain in URL");
            throw new IllegalArgumentException("Invalid URL: missing domain");
        }

        // Check if valid URL format
        try {
            new URI(url);
        } catch (URISyntaxException e) {
            log.warn("Validation failed: Invalid URL format - {}", e.getMessage());
            throw new IllegalArgumentException("Invalid URL format");
        }

        // Check for duplicates
        if (linkRepository.findByLongUrl(url).isPresent()) {
            log.warn("Validation failed: URL already exists: {}", url);
            throw new IllegalArgumentException("URL already shortened");
        }

        log.debug("URL validation successful");
    }


    public URI getTarget(String code) {
        log.info("Looking up target URL for short code: {}", code);

        // Check cache first
        Optional<String> cachedUrl = redisCacheService.getLongUrlByShortCode(code);
        if (cachedUrl.isPresent()) {
            return URI.create(cachedUrl.get());
        }

        log.debug("Cache miss for short code: {}, checking database", code);
        long shortLinkId = Base62Encoder.decode(code);
        log.debug("Decoded short code to ID: {}", shortLinkId);

        try {
            ShortLink shortLink = linkRepository.findById(shortLinkId)
                    .orElseThrow(() -> new IllegalArgumentException("Link not found"));

            // Cache the result for future requests
            redisCacheService.cacheShortLink(shortLink);

            URI longUrl = URI.create(shortLink.getLongUrl());
            log.info("Found target URL: {} for short code: {}", longUrl, code);
            return longUrl;
        } catch (IllegalArgumentException e) {
            log.warn("Short link not found for code: {} - {}", code, e.getMessage());
            return URI.create("/error");
        }
    }

}
