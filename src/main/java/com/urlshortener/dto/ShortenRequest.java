package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ShortenRequest {

    @NotBlank(message = "Original URL is required")
    @Size(max = 2048, message = "URL length cannot exceed 2048 characters")
    private String originalUrl;

    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,30}$", message = "Custom alias must be 3-30 alphanumeric characters, dashes or underscores", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String customAlias;

    private Integer expirationDays;

    public ShortenRequest() {
    }

    public ShortenRequest(String originalUrl, String customAlias, Integer expirationDays) {
        this.originalUrl = originalUrl;
        this.customAlias = customAlias;
        this.expirationDays = expirationDays;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getCustomAlias() {
        return customAlias;
    }

    public void setCustomAlias(String customAlias) {
        this.customAlias = customAlias;
    }

    public Integer getExpirationDays() {
        return expirationDays;
    }

    public void setExpirationDays(Integer expirationDays) {
        this.expirationDays = expirationDays;
    }
}

