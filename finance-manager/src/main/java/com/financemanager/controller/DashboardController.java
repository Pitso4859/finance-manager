package com.financemanager.controller;

import com.financemanager.dto.DashboardSummary;
import com.financemanager.model.User;
import com.financemanager.repository.UserRepository;
import com.financemanager.service.impl.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Dashboard controller — the main landing page after login.
 * Thin controller: delegates all logic to DashboardService.
 *
 * SE Practice: Controllers are thin — they only handle HTTP concerns
 * (routing, model binding, view selection). All business logic lives in services.
 */
@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    @GetMapping
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow();

        DashboardSummary summary = dashboardService.buildDashboard(user);

        model.addAttribute("summary", summary);
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Dashboard");

        return "dashboard/index";
    }
}
