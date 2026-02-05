package com.sanuth.shortme.controller;

import com.sanuth.shortme.service.LinkService;
import com.sanuth.shortme.service.RedisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirectController {

    private final LinkService linkService;
    private final RedisService redisService;

    public RedirectController(LinkService linkService, RedisService redisService) {
        this.linkService = linkService;
        this.redisService = redisService;
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        URI target = linkService.getTarget(code);

        if (!target.getPath().equals("/error")) {
            redisService.incrementClick(code);
        }

        return ResponseEntity.status(HttpStatus.FOUND).location(target).build();
    }
}
