package com.financemanager.controller;

import com.financemanager.model.Transaction;
import com.financemanager.model.User;
import com.financemanager.patterns.strategy.ReportStrategy;
import com.financemanager.repository.UserRepository;
import com.financemanager.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Report controller — demonstrates the Strategy pattern in action.
 * The client (this controller) selects a strategy at runtime based on
 * the user's selection. No if/else — just a map lookup.
 */
@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final Map<String, ReportStrategy> strategies;

    @GetMapping
    public String reports(@RequestParam(defaultValue = "monthlySummaryStrategy") String type,
                          @RequestParam(defaultValue = "6") int months,
                          @AuthenticationPrincipal UserDetails userDetails,
                          Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusMonths(months);

        List<Transaction> transactions = transactionService.findByUserAndDateRange(user, start, end);

        // Strategy pattern — no if/else, just select and execute
        ReportStrategy strategy = strategies.getOrDefault(type, strategies.get("monthlySummaryStrategy"));
        ReportStrategy.ReportResult result = strategy.generate(transactions);

        // Create a list of strategy info for the dropdown
        List<StrategyInfo> strategyInfos = strategies.entrySet().stream()
                .map(entry -> new StrategyInfo(entry.getKey(), entry.getValue().getName()))
                .toList();

        model.addAttribute("result", result);
        model.addAttribute("strategies", strategyInfos);
        model.addAttribute("selectedType", type);
        model.addAttribute("months", months);
        model.addAttribute("pageTitle", "Reports");

        return "report/index";
    }

    // Helper record for strategy dropdown
    public record StrategyInfo(String key, String name) {}
}