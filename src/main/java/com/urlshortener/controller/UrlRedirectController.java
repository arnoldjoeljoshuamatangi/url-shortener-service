package com.urlshortener.controller;

import com.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;
import java.util.Set;

@Controller
@Hidden
public class UrlRedirectController {

    private final UrlService urlService;

    private static final Set<String> RESERVED_KEYWORDS = Set.of(
            "api", "swagger-ui", "v3", "actuator", "error", "css", "js",
            "images", "favicon.ico", "index.html", "h2-console"
    );

    public UrlRedirectController(UrlService urlService) {
        this.urlService = urlService;
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9_-]+}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            HttpServletRequest request) {

        if (RESERVED_KEYWORDS.contains(shortCode.toLowerCase())) {
            return ResponseEntity.notFound().build();
        }

        String originalUrl = urlService.resolveUrlAndTrack(shortCode, request);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}

