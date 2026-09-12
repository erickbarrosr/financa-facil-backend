package com.financafacil.presentation.controller;

import com.financafacil.domain.port.in.DashboardUseCase;
import com.financafacil.presentation.dto.response.ApiResponse;
import com.financafacil.presentation.dto.response.CategoryBreakdownResponse;
import com.financafacil.presentation.dto.response.DashboardSummaryResponse;
import com.financafacil.presentation.dto.response.MonthlySeriesResponse;
import com.financafacil.presentation.dto.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardUseCase dashboardUseCase;

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> getSummary(
            @AuthenticationPrincipal Object principal,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month) {
        var now = LocalDate.now();
        int y = year == 0 ? now.getYear() : year;
        int m = month == 0 ? now.getMonthValue() : month;
        return ApiResponse.ok(dashboardUseCase.getSummary(toUuid(principal), y, m));
    }

    @GetMapping("/monthly-series")
    public ApiResponse<List<MonthlySeriesResponse>> getMonthlySeries(
            @AuthenticationPrincipal Object principal,
            @RequestParam(defaultValue = "6") int months) {
        return ApiResponse.ok(dashboardUseCase.getMonthlySeries(toUuid(principal), months));
    }

    @GetMapping("/by-category")
    public ApiResponse<List<CategoryBreakdownResponse>> getByCategory(
            @AuthenticationPrincipal Object principal,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "expense") String type) {
        var now = LocalDate.now();
        int y = year == 0 ? now.getYear() : year;
        int m = month == 0 ? now.getMonthValue() : month;
        return ApiResponse.ok(dashboardUseCase.getByCategory(toUuid(principal), y, m, type));
    }

    @GetMapping("/recent-transactions")
    public ApiResponse<List<TransactionResponse>> getRecentTransactions(
            @AuthenticationPrincipal Object principal,
            @RequestParam(defaultValue = "6") int limit) {
        return ApiResponse.ok(dashboardUseCase.getRecentTransactions(toUuid(principal), limit));
    }

    private UUID toUuid(Object principal) {
        return UUID.fromString(principal.toString());
    }
}
