package com.urlshortener.dto;

import java.time.LocalDateTime;

public class ClickRecordDto {

    private LocalDateTime timestamp;
    private String ipAddress;
    private String browser;
    private String os;
    private String deviceType;
    private String referer;
    private String country;

    public ClickRecordDto() {
    }

    public ClickRecordDto(LocalDateTime timestamp, String ipAddress, String browser, String os,
                          String deviceType, String referer, String country) {
        this.timestamp = timestamp;
        this.ipAddress = ipAddress;
        this.browser = browser;
        this.os = os;
        this.deviceType = deviceType;
        this.referer = referer;
        this.country = country;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}

