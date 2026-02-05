package com.sanuth.shortme;

import com.sanuth.shortme.model.db.ShortLink;
import com.sanuth.shortme.model.dto.CreateShortLinkRequest;
import com.sanuth.shortme.model.dto.ShortLinkResponse;
import com.sanuth.shortme.repository.LinkRepository;
import com.sanuth.shortme.service.LinkService;
import com.sanuth.shortme.service.RedisCacheService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class LinkServiceTest {

    @Autowired
    LinkService linkService;

    @MockitoBean
    LinkRepository linkRepository;

    @MockitoBean
    RedisCacheService redisCacheService;

    @Test
    void testEmptyUrl(){
        assertThrows(IllegalArgumentException.class,
                () -> linkService.createShortLink(new CreateShortLinkRequest(null, null, null)));
    }

    @Test
    void testEmptyStringUrl(){
        assertThrows(IllegalArgumentException.class,
                () -> linkService.createShortLink(new CreateShortLinkRequest("", null, null)));
    }

    @Test
    void Urlwithouthttp(){
        ShortLink saved = new ShortLink();
        saved.setId(1L);
        saved.setLongUrl("local.com");
        saved.setStatus(com.sanuth.shortme.model.db.LinkStatus.ACTIVE);
        when(linkRepository.save(any())).thenReturn(saved);

        ShortLinkResponse response = linkService.createShortLink(new CreateShortLinkRequest("local.com", "", null));

        // validateUrl prepends https:// — verify that's the URL it passed to the duplicate check
        verify(linkRepository).findByLongUrl("https://local.com");

        assertNotNull(response);
        assertNotNull(response.getShortCode());
    }

    @Test
    void validateDoublePrefix(){
        assertThrows(IllegalArgumentException.class,()->linkService.createShortLink(new CreateShortLinkRequest("https://https://example.com", "", null)));
    }

    @Test
    void validatesuffix(){
        assertThrows(IllegalArgumentException.class,()->linkService.createShortLink(new CreateShortLinkRequest("https://example", "", null)));

    }



}
