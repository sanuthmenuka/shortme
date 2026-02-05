package com.sanuth.shortme;

import com.sanuth.shortme.controller.ApiController;
import com.sanuth.shortme.controller.RedirectController;
import com.sanuth.shortme.model.db.LinkStatus;
import com.sanuth.shortme.model.db.ShortLink;
import com.sanuth.shortme.model.dto.ShortLinkResponse;
import com.sanuth.shortme.service.LinkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {ApiController.class, RedirectController.class})
public class RestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LinkService linkService;

    private ShortLinkResponse sampleResponse;

    @BeforeEach
    void setUp() {
        ShortLink shortLink = new ShortLink();
        shortLink.setId(1L);
        shortLink.setShortCode("abc123");
        shortLink.setLongUrl("https://example.com");
        shortLink.setCreatedAt(Instant.parse("2026-02-05T10:00:00Z"));
        shortLink.setExpiresAt(Instant.parse("2026-03-05T10:00:00Z"));
        shortLink.setStatus(LinkStatus.ACTIVE);

        sampleResponse = new ShortLinkResponse(shortLink);
    }

    // ---------------------------------------------------------------
    // POST /api/links
    // ---------------------------------------------------------------

    @Test
    void createShortLink_validRequest_returns201() throws Exception {
        when(linkService.createShortLink(any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "longUrl": "https://example.com",
                                  "customShortCode": "abc123",
                                  "expiresAt": "2026-03-05T10:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Short link created successfully"))
                .andExpect(jsonPath("$.data.shortCode").value("abc123"))
                .andExpect(jsonPath("$.data.longUrl").value("https://example.com"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void createShortLink_withoutOptionalFields_returns201() throws Exception {
        when(linkService.createShortLink(any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "longUrl": "https://example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createShortLink_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createShortLink_missingLongUrl_returns400() throws Exception {
        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customShortCode": "abc123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createShortLink_invalidUrlScheme_returns400() throws Exception {
        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "longUrl": "ftp://example.com"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createShortLink_shortCodeTooShort_returns400() throws Exception {
        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "longUrl": "https://example.com",
                                  "customShortCode": "ab"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createShortLink_shortCodeInvalidChars_returns400() throws Exception {
        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "longUrl": "https://example.com",
                                  "customShortCode": "abc!@#"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createShortLink_duplicateUrl_returns400() throws Exception {
        when(linkService.createShortLink(any()))
                .thenThrow(new IllegalArgumentException("URL already shortened"));

        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "longUrl": "https://example.com"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("URL already shortened"));
    }

    @Test
    void createShortLink_duplicateShortCode_returns400() throws Exception {
        when(linkService.createShortLink(any()))
                .thenThrow(new IllegalArgumentException("Short code 'abc123' is already in use"));

        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "longUrl": "https://example.com",
                                  "customShortCode": "abc123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Short code 'abc123' is already in use"));
    }

    @Test
    void createShortLink_serviceThrowsGenericException_returns500() throws Exception {
        when(linkService.createShortLink(any()))
                .thenThrow(new RuntimeException("Something went wrong"));

        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "longUrl": "https://example.com"
                                }
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ---------------------------------------------------------------
    // GET /api/links/{id}
    // ---------------------------------------------------------------

    @Test
    void getById_returns200() throws Exception {
        mockMvc.perform(get("/api/links/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ---------------------------------------------------------------
    // PUT /api/links/{id}
    // ---------------------------------------------------------------

    @Test
    void update_validRequest_returns200() throws Exception {
        mockMvc.perform(put("/api/links/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "longUrl": "https://updated-example.com",
                                  "customShortCode": "upd123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Short link updated successfully"));
    }

    @Test
    void update_invalidBody_returns400() throws Exception {
        mockMvc.perform(put("/api/links/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "longUrl": "not-a-url"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ---------------------------------------------------------------
    // DELETE /api/links/{id}
    // ---------------------------------------------------------------

    @Test
    void delete_returns200() throws Exception {
        mockMvc.perform(delete("/api/links/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Short link deleted successfully"));
    }

    // ---------------------------------------------------------------
    // GET /{code} — redirect
    // ---------------------------------------------------------------

    @Test
    void redirect_validCode_returns302() throws Exception {
        when(linkService.getTarget("abc123"))
                .thenReturn(URI.create("https://example.com"));

        mockMvc.perform(get("/abc123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com"));
    }

    @Test
    void redirect_unknownCode_redirectsToErrorPage() throws Exception {
        when(linkService.getTarget("missing"))
                .thenReturn(URI.create("/error"));

        mockMvc.perform(get("/missing"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/error"));
    }

    @Test
    void root_path_returns404() throws Exception {
        mockMvc.perform(get("/undefined"))
                .andExpect(status().isNotFound());  // 404
    }


}
