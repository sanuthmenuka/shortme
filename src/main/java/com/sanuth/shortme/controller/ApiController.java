package com.sanuth.shortme.controller;

import com.sanuth.shortme.model.dto.ApiResponse;
import com.sanuth.shortme.model.dto.CreateShortLinkRequest;
import com.sanuth.shortme.model.dto.ShortLinkResponse;
import com.sanuth.shortme.service.LinkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/links")
public class ApiController {

    private final LinkService linkService;

    public ApiController(LinkService linkService) {
        this.linkService = linkService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<String>> home(){
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Success, Welcome to shortme"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShortLinkResponse>> create(
            @Valid @RequestBody CreateShortLinkRequest request) {
        ShortLinkResponse response = linkService.createShortLink(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Short link created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShortLinkResponse>> getById(@PathVariable Long id) {
        // TODO: Implement get by ID logic
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ShortLinkResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateShortLinkRequest request) {
        // TODO: Implement update logic
        return ResponseEntity.ok(ApiResponse.success("Short link updated successfully", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        // TODO: Implement delete logic
        return ResponseEntity.ok(ApiResponse.success("Short link deleted successfully", null));
    }
}
