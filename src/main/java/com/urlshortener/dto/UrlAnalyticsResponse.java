package com.urlshortener.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class UrlAnalyticsResponse {

    private String shortCode;
    private String originalUrl;
    private String shortUrl;
    private long totalClicks;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Map<String, Long> clicksByDate;
    private Map<String, Long> deviceBreakdown;
    private Map<String, Long> browserBreakdown;
    private Map<String, Long> refererBreakdown;
    private List<ClickRecordDto> recentClicks;

    public UrlAnalyticsResponse() {
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public long getTotalClicks() {
        return totalClicks;
    }

    public void setTotalClicks(long totalClicks) {
        this.totalClicks = totalClicks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Map<String, Long> getClicksByDate() {
        return clicksByDate;
    }

    public void setClicksByDate(Map<String, Long> clicksByDate) {
        this.clicksByDate = clicksByDate;
    }

    public Map<String, Long> getDeviceBreakdown() {
        return deviceBreakdown;
    }

    public void setDeviceBreakdown(Map<String, Long> deviceBreakdown) {
        this.deviceBreakdown = deviceBreakdown;
    }

    public Map<String, Long> getBrowserBreakdown() {
        return browserBreakdown;
    }

    public void setBrowserBreakdown(Map<String, Long> browserBreakdown) {
        this.browserBreakdown = browserBreakdown;
    }

    public Map<String, Long> getRefererBreakdown() {
        return refererBreakdown;
    }

    public void setRefererBreakdown(Map<String, Long> refererBreakdown) {
        this.refererBreakdown = refererBreakdown;
    }

    public List<ClickRecordDto> getRecentClicks() {
        return recentClicks;
    }

    public void setRecentClicks(List<ClickRecordDto> recentClicks) {
        this.recentClicks = recentClicks;
    }
}

