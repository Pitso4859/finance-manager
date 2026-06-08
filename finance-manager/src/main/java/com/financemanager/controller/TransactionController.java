package com.financemanager.controller;

import com.financemanager.dto.TransactionRequest;
import com.financemanager.model.Transaction;
import com.financemanager.model.User;
import com.financemanager.repository.UserRepository;
import com.financemanager.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Transaction CRUD controller.
 *
 * SE Practices:
 * - @Valid + BindingResult for form validation with user-friendly error messages
 * - PRG pattern (Post-Redirect-Get) to prevent duplicate form submissions
 * - RedirectAttributes for flash messages (success/error toasts)
 * - Pagination via Spring Data Pageable
 */
@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    // ── List (paginated) ─────────────────────────────────────────────

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @AuthenticationPrincipal UserDetails userDetails,
                       Model model) {
        User user = getUser(userDetails);
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> txPage = transactionService.findPagedByUser(user, pageable);

        model.addAttribute("transactions", txPage);
        model.addAttribute("categories", Transaction.Category.values());
        model.addAttribute("types", Transaction.TransactionType.values());
        model.addAttribute("pageTitle", "Transactions");
        return "transaction/list";
    }

    // ── Create ───────────────────────────────────────────────────────

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("request", new TransactionRequest(
            null, null, null, null, null, null));
        model.addAttribute("categories", Transaction.Category.values());
        model.addAttribute("types", Transaction.TransactionType.values());
        model.addAttribute("pageTitle", "Add Transaction");
        return "transaction/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("request") TransactionRequest request,
                         BindingResult result,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", Transaction.Category.values());
            model.addAttribute("types", Transaction.TransactionType.values());
            return "transaction/form";
        }

        try {
            User user = getUser(userDetails);
            transactionService.create(request, user);
            redirectAttributes.addFlashAttribute("successMessage", "Transaction added successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/transactions"; // PRG pattern
    }

    // ── Edit ─────────────────────────────────────────────────────────

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        User user = getUser(userDetails);
        Transaction tx = transactionService.findByIdAndUser(id, user);

        TransactionRequest request = new TransactionRequest(
            tx.getDescription(), tx.getAmount(), tx.getType(),
            tx.getCategory(), tx.getTransactionDate(), tx.getNotes());

        model.addAttribute("request", request);
        model.addAttribute("transactionId", id);
        model.addAttribute("categories", Transaction.Category.values());
        model.addAttribute("types", Transaction.TransactionType.values());
        model.addAttribute("pageTitle", "Edit Transaction");
        return "transaction/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("request") TransactionRequest request,
                         BindingResult result,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", Transaction.Category.values());
            model.addAttribute("types", Transaction.TransactionType.values());
            model.addAttribute("transactionId", id);
            return "transaction/form";
        }

        User user = getUser(userDetails);
        transactionService.update(id, request, user);
        redirectAttributes.addFlashAttribute("successMessage", "Transaction updated.");
        return "redirect:/transactions";
    }

    // ── Delete ───────────────────────────────────────────────────────

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        User user = getUser(userDetails);
        transactionService.delete(id, user);
        redirectAttributes.addFlashAttribute("successMessage", "Transaction deleted.");
        return "redirect:/transactions";
    }

    // ── Helper ───────────────────────────────────────────────────────

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
    }
}
