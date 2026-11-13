package com.urlshortener.service;

import com.urlshortener.dto.*;
import com.urlshortener.entity.ClickAnalytics;
import com.urlshortener.entity.Url;
import com.urlshortener.exception.DuplicateAliasException;
import com.urlshortener.exception.InvalidUrlException;
import com.urlshortener.exception.UrlExpiredException;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.repository.ClickAnalyticsRepository;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.Base62Encoder;
import com.urlshortener.util.QrCodeGenerator;
import com.urlshortener.util.UserAgentParser;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UrlService {

    private static final Logger log = LoggerFactory.getLogger(UrlService.class);

    private final UrlRepository urlRepository;
    private final ClickAnalyticsRepository analyticsRepository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public UrlService(UrlRepository urlRepository, ClickAnalyticsRepository analyticsRepository) {
        this.urlRepository = urlRepository;
        this.analyticsRepository = analyticsRepository;
    }

    @Transactional
    public ShortenResponse shortenUrl(ShortenRequest request, HttpServletRequest httpRequest) {
        String originalUrl = request.getOriginalUrl().trim();

        // Normalize URL
        if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
            originalUrl = "https://" + originalUrl;
        }

        // Validate URL format
        try {
            URI uri = new URI(originalUrl);
            if (uri.getHost() == null) {
                throw new InvalidUrlException("Invalid URL: Host cannot be empty");
            }
        } catch (Exception e) {
            throw new InvalidUrlException("Malformed URL format: " + originalUrl);
        }

        String shortCode;
        String customAlias = request.getCustomAlias();

        if (customAlias != null && !customAlias.isBlank()) {
            customAlias = customAlias.trim();
            if (urlRepository.existsByShortCode(customAlias)) {
                throw new DuplicateAliasException("Custom alias '" + customAlias + "' is already in use");
            }
            shortCode = customAlias;
        } else {
            // Generate unique random base62 code
            do {
                shortCode = Base62Encoder.generateRandomCode();
            } while (urlRepository.existsByShortCode(shortCode));
        }

        LocalDateTime expiresAt = null;
        if (request.getExpirationDays() != null && request.getExpirationDays() > 0) {
            expiresAt = LocalDateTime.now().plusDays(request.getExpirationDays());
        }

        Url url = new Url(originalUrl, shortCode, customAlias, expiresAt);
        urlRepository.save(url);

        String fullBaseUrl = determineBaseUrl(httpRequest);
        String fullShortUrl = fullBaseUrl + "/" + shortCode;
        String qrCodeUrl = fullBaseUrl + "/api/v1/urls/" + shortCode + "/qr";

        return new ShortenResponse(
                url.getOriginalUrl(),
                fullShortUrl,
                url.getShortCode(),
                qrCodeUrl,
                url.getCreatedAt(),
                url.getExpiresAt(),
                url.getClickCount()
        );
    }

    @Transactional
    public String resolveUrlAndTrack(String shortCode, HttpServletRequest request) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short URL with code '" + shortCode + "' not found"));

        if (!url.isActive()) {
            throw new UrlNotFoundException("This short URL has been disabled");
        }

        if (url.isExpired()) {
            throw new UrlExpiredException("This short URL has expired on " + url.getExpiresAt());
        }

        // Increment click count
        url.incrementClicks();
        urlRepository.save(url);

        // Record click analytics asynchronously
        recordAnalytics(shortCode, request);

        return url.getOriginalUrl();
    }

    @Async
    public void recordAnalytics(String shortCode, HttpServletRequest request) {
        try {
            String ipAddress = extractClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            String referer = request.getHeader("Referer");

            String browser = UserAgentParser.parseBrowser(userAgent);
            String os = UserAgentParser.parseOs(userAgent);
            String deviceType = UserAgentParser.parseDeviceType(userAgent);

            ClickAnalytics analytics = new ClickAnalytics(
                    shortCode,
                    ipAddress,
                    userAgent,
                    referer,
                    browser,
                    os,
                    deviceType,
                    "Unknown"
            );
            analyticsRepository.save(analytics);
        } catch (Exception e) {
            log.error("Failed to record analytics for code: {}", shortCode, e);
        }
    }

    @Transactional(readOnly = true)
    public UrlAnalyticsResponse getAnalytics(String shortCode, HttpServletRequest request) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short URL with code '" + shortCode + "' not found"));

        String fullBaseUrl = determineBaseUrl(request);
        String fullShortUrl = fullBaseUrl + "/" + shortCode;

        UrlAnalyticsResponse response = new UrlAnalyticsResponse();
        response.setShortCode(url.getShortCode());
        response.setOriginalUrl(url.getOriginalUrl());
        response.setShortUrl(fullShortUrl);
        response.setTotalClicks(url.getClickCount());
        response.setCreatedAt(url.getCreatedAt());
        response.setExpiresAt(url.getExpiresAt());

        // Device breakdown
        Map<String, Long> deviceMap = new HashMap<>();
        for (Object[] row : analyticsRepository.countClicksByDeviceType(shortCode)) {
            deviceMap.put(String.valueOf(row[0]), (Long) row[1]);
        }
        response.setDeviceBreakdown(deviceMap);

        // Browser breakdown
        Map<String, Long> browserMap = new HashMap<>();
        for (Object[] row : analyticsRepository.countClicksByBrowser(shortCode)) {
            browserMap.put(String.valueOf(row[0]), (Long) row[1]);
        }
        response.setBrowserBreakdown(browserMap);

        // Referer breakdown
        Map<String, Long> refererMap = new HashMap<>();
        for (Object[] row : analyticsRepository.countClicksByReferer(shortCode)) {
            refererMap.put(String.valueOf(row[0]), (Long) row[1]);
        }
        response.setRefererBreakdown(refererMap);

        // Daily clicks
        Map<String, Long> dailyMap = new LinkedHashMap<>();
        for (Object[] row : analyticsRepository.countClicksByDate(shortCode)) {
            dailyMap.put(String.valueOf(row[0]), (Long) row[1]);
        }
        response.setClicksByDate(dailyMap);

        // Recent clicks
        List<ClickAnalytics> recent = analyticsRepository.findTop100ByShortCodeOrderByClickedAtDesc(shortCode);
        List<ClickRecordDto> dtoList = recent.stream().map(c -> new ClickRecordDto(
                c.getClickedAt(),
                c.getIpAddress(),
                c.getBrowser(),
                c.getOs(),
                c.getDeviceType(),
                c.getReferer(),
                c.getCountry()
        )).collect(Collectors.toList());
        response.setRecentClicks(dtoList);

        return response;
    }

    @Transactional(readOnly = true)
    public List<ShortenResponse> getAllUrls(HttpServletRequest request) {
        String fullBaseUrl = determineBaseUrl(request);
        return urlRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(u -> new ShortenResponse(
                        u.getOriginalUrl(),
                        fullBaseUrl + "/" + u.getShortCode(),
                        u.getShortCode(),
                        fullBaseUrl + "/api/v1/urls/" + u.getShortCode() + "/qr",
                        u.getCreatedAt(),
                        u.getExpiresAt(),
                        u.getClickCount()
                )).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SystemStatsResponse getSystemStats() {
        long totalUrls = urlRepository.count();
        long activeUrls = urlRepository.countByActiveTrue();
        Long totalClicks = urlRepository.sumTotalClicks();
        return new SystemStatsResponse(totalUrls, totalClicks != null ? totalClicks : 0L, activeUrls);
    }

    @Transactional
    public void deleteUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short URL with code '" + shortCode + "' not found"));
        analyticsRepository.deleteByShortCode(shortCode);
        urlRepository.delete(url);
    }

    public byte[] generateQrCode(String shortCode, int width, int height, HttpServletRequest request) {
        if (!urlRepository.existsByShortCode(shortCode)) {
            throw new UrlNotFoundException("Short URL with code '" + shortCode + "' not found");
        }
        String fullUrl = determineBaseUrl(request) + "/" + shortCode;
        return QrCodeGenerator.generateQrCodeImage(fullUrl, width, height);
    }

    private String determineBaseUrl(HttpServletRequest request) {
        if (baseUrl != null && !baseUrl.equals("http://localhost:8080")) {
            return baseUrl;
        }
        if (request != null) {
            String scheme = request.getScheme();
            String host = request.getServerName();
            int port = request.getServerPort();
            if ((scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443)) {
                return scheme + "://" + host;
            }
            return scheme + "://" + host + ":" + port;
        }
        return baseUrl;
    }

    private String extractClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }
}

