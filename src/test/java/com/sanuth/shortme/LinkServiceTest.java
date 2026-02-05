package com.sanuth.shortme;

import com.sanuth.shortme.model.db.LinkStatus;
import com.sanuth.shortme.model.db.ShortLink;
import com.sanuth.shortme.model.dto.CreateShortLinkRequest;
import com.sanuth.shortme.model.dto.ShortLinkResponse;
import com.sanuth.shortme.repository.LinkRepository;
import com.sanuth.shortme.service.LinkService;
import com.sanuth.shortme.service.RedisCacheService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    // ---------------------------------------------------------------
    // Concurrency tests
    // ---------------------------------------------------------------

    private static final Logger log = LoggerFactory.getLogger(LinkServiceTest.class);


    // Two threads race to save the same custom short code simultaneously.
    // The barrier holds both threads at save() so they hit the DB at the same time.
    // First one through succeeds; second one gets DataIntegrityViolationException
    // from the unique constraint, which the service translates to IllegalArgumentException.
    @Test
    void concurrentDuplicateShortCode_secondThreadRejected() throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(2);
        AtomicInteger saveCount = new AtomicInteger(0);
        AtomicInteger exceptionCount = new AtomicInteger(0);

        when(linkRepository.save(any())).thenAnswer(invocation -> {
            ShortLink entity = invocation.getArgument(0);
            int count = saveCount.incrementAndGet();
            log.info("[Race] Thread {} — save called (count: {}), shortCode={}, longUrl={}",
                    Thread.currentThread().getName(), count, entity.getShortCode(), entity.getLongUrl());

            barrier.await(); // both threads arrive here before either continues

            // first thread to increment past the barrier wins; second one simulates the DB rejecting the duplicate
            if (count > 1) {
                log.info("[Race] Thread {} — simulating unique constraint violation", Thread.currentThread().getName());
                throw new DataIntegrityViolationException("unique constraint violation on shortCode");
            }

            log.info("[Race] Thread {} — save succeeding", Thread.currentThread().getName());
            ShortLink saved = new ShortLink();
            saved.setId(1L);
            saved.setLongUrl(entity.getLongUrl());
            saved.setShortCode(entity.getShortCode());
            saved.setStatus(LinkStatus.ACTIVE);
            return saved;
        });

        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> {
            log.info("[Race] Thread {} — submitting url-a.example.com with code 'clash'", Thread.currentThread().getName());
            try {
                linkService.createShortLink(new CreateShortLinkRequest("https://url-a.example.com", "clash", null));
                log.info("[Race] Thread {} — succeeded", Thread.currentThread().getName());
            } catch (IllegalArgumentException e) {
                log.info("[Race] Thread {} — rejected: {}", Thread.currentThread().getName(), e.getMessage());
                exceptionCount.incrementAndGet();
            }
        });
        executor.submit(() -> {
            log.info("[Race] Thread {} — submitting url-b.example.com with code 'clash'", Thread.currentThread().getName());
            try {
                linkService.createShortLink(new CreateShortLinkRequest("https://url-b.example.com", "clash", null));
                log.info("[Race] Thread {} — succeeded", Thread.currentThread().getName());
            } catch (IllegalArgumentException e) {
                log.info("[Race] Thread {} — rejected: {}", Thread.currentThread().getName(), e.getMessage());
                exceptionCount.incrementAndGet();
            }
        });

        executor.shutdown();
        executor.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);

        log.info("[Race] save() called {} times, exceptions caught: {}", saveCount.get(), exceptionCount.get());
        assertEquals(2, saveCount.get(), "Both threads should have attempted save");
        assertEquals(1, exceptionCount.get(), "Exactly one thread should have been rejected");
    }
}
