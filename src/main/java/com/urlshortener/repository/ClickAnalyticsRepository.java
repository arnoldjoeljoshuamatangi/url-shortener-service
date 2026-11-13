package com.urlshortener.repository;

import com.urlshortener.entity.ClickAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClickAnalyticsRepository extends JpaRepository<ClickAnalytics, Long> {

    List<ClickAnalytics> findTop100ByShortCodeOrderByClickedAtDesc(String shortCode);

    long countByShortCode(String shortCode);

    @Query("SELECT c.deviceType, COUNT(c) FROM ClickAnalytics c WHERE c.shortCode = :shortCode GROUP BY c.deviceType")
    List<Object[]> countClicksByDeviceType(@Param("shortCode") String shortCode);

    @Query("SELECT c.browser, COUNT(c) FROM ClickAnalytics c WHERE c.shortCode = :shortCode GROUP BY c.browser")
    List<Object[]> countClicksByBrowser(@Param("shortCode") String shortCode);

    @Query("SELECT c.referer, COUNT(c) FROM ClickAnalytics c WHERE c.shortCode = :shortCode GROUP BY c.referer ORDER BY COUNT(c) DESC")
    List<Object[]> countClicksByReferer(@Param("shortCode") String shortCode);

    @Query("SELECT FUNCTION('DATE', c.clickedAt), COUNT(c) FROM ClickAnalytics c WHERE c.shortCode = :shortCode GROUP BY FUNCTION('DATE', c.clickedAt) ORDER BY FUNCTION('DATE', c.clickedAt) ASC")
    List<Object[]> countClicksByDate(@Param("shortCode") String shortCode);

    void deleteByShortCode(String shortCode);
}

