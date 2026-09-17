package com.kdlt.platform.analytics.controller;

import com.kdlt.platform.analytics.dto.DashboardSummaryDto;
import com.kdlt.platform.analytics.dto.RevenueByMonthDto;
import com.kdlt.platform.analytics.dto.TopProductDto;
import com.kdlt.platform.analytics.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getSummary() {
        return ResponseEntity.ok(analyticsService.getDashboardSummary());
    }

    @GetMapping("/revenue-by-month")
    public ResponseEntity<List<RevenueByMonthDto>> getRevenueByMonth() {
        return ResponseEntity.ok(analyticsService.getRevenueByMonth());
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<TopProductDto>> getTopProducts(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(analyticsService.getTopProducts(limit));
    }
}