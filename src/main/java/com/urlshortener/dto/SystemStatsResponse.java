package com.urlshortener.dto;

public class SystemStatsResponse {

    private long totalUrls;
    private long totalClicks;
    private long activeUrls;

    public SystemStatsResponse() {
    }

    public SystemStatsResponse(long totalUrls, long totalClicks, long activeUrls) {
        this.totalUrls = totalUrls;
        this.totalClicks = totalClicks;
        this.activeUrls = activeUrls;
    }

    public long getTotalUrls() {
        return totalUrls;
    }

    public void setTotalUrls(long totalUrls) {
        this.totalUrls = totalUrls;
    }

    public long getTotalClicks() {
        return totalClicks;
    }

    public void setTotalClicks(long totalClicks) {
        this.totalClicks = totalClicks;
    }

    public long getActiveUrls() {
        return activeUrls;
    }

    public void setActiveUrls(long activeUrls) {
        this.activeUrls = activeUrls;
    }
}

