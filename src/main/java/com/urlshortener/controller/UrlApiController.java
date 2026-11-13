package com.urlshortener.controller;

import com.urlshortener.dto.*;
import com.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/urls")
@Tag(name = "URL Management", description = "Endpoints for creating, managing, and analyzing shortened URLs")
@CrossOrigin(origins = "*")
public class UrlApiController {

    private final UrlService urlService;

    public UrlApiController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    @Operation(summary = "Create a shortened URL", description = "Generates a unique short URL with optional custom alias and expiration date")
    public ResponseEntity<ApiResponse<ShortenResponse>> shortenUrl(
            @Valid @RequestBody ShortenRequest request,
            HttpServletRequest httpRequest) {
        ShortenResponse response = urlService.shortenUrl(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Short URL created successfully", response));
    }

    @GetMapping
    @Operation(summary = "List recent URLs", description = "Retrieves the most recently created short URLs")
    public ResponseEntity<ApiResponse<List<ShortenResponse>>> getAllUrls(HttpServletRequest request) {
        List<ShortenResponse> urls = urlService.getAllUrls(request);
        return ResponseEntity.ok(ApiResponse.success(urls));
    }

    @GetMapping("/{shortCode}/analytics")
    @Operation(summary = "Get URL click analytics", description = "Fetches total clicks, geographic/device/browser breakdowns, and click timeline")
    public ResponseEntity<ApiResponse<UrlAnalyticsResponse>> getAnalytics(
            @PathVariable String shortCode,
            HttpServletRequest request) {
        UrlAnalyticsResponse analytics = urlService.getAnalytics(shortCode, request);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping(value = "/{shortCode}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Generate QR Code", description = "Generates a high-resolution PNG QR Code image for the shortened URL")
    public ResponseEntity<byte[]> getQrCode(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "300") int size,
            HttpServletRequest request) {
        byte[] qrBytes = urlService.generateQrCode(shortCode, size, size, request);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(qrBytes.length);
        headers.setCacheControl("max-age=86400");
        return new ResponseEntity<>(qrBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/stats/overview")
    @Operation(summary = "Get system statistics", description = "Returns total URLs, total clicks recorded, and active links")
    public ResponseEntity<ApiResponse<SystemStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(urlService.getSystemStats()));
    }

    @DeleteMapping("/{shortCode}")
    @Operation(summary = "Delete shortened URL", description = "Permanently deletes a short URL and all associated click analytics")
    public ResponseEntity<ApiResponse<Void>> deleteUrl(@PathVariable String shortCode) {
        urlService.deleteUrl(shortCode);
        return ResponseEntity.ok(ApiResponse.success("URL and analytics deleted successfully", null));
    }
}

