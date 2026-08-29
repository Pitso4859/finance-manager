package com.financemanager.service;

import com.financemanager.exception.ResourceNotFoundException;
import com.financemanager.exception.ValidationException;
import com.financemanager.model.Budget;
import com.financemanager.model.Category;
import com.financemanager.model.User;
import com.financemanager.repository.BudgetRepository;
import com.financemanager.util.Validation;

import java.math.BigDecimal;
import java.util.List;

public final class BudgetService {
    private final BudgetRepository repository;
    public BudgetService(BudgetRepository repository) { this.repository = repository; }

    public Budget create(User user, Category category, String amount, int month, int year) {
        validateCategory(category);
        BigDecimal limit = Validation.positiveAmount(amount, "Budget limit");
        Validation.month(month); Validation.year(year);
        if (repository.existsForCategoryAndPeriod(user.getId(), null, category, month, year))
            throw new ValidationException("A budget already exists for this category and month.");
        return repository.save(new Budget(user.getId(), category, limit, month, year));
    }

    public Budget update(User user, String id, Category category, String amount, int month, int year) {
        Budget budget = repository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget was not found."));
        validateCategory(category);
        if (repository.existsForCategoryAndPeriod(user.getId(), id, category, month, year))
            throw new ValidationException("A budget already exists for this category and month.");
        budget.setCategory(category); budget.setLimitAmount(Validation.positiveAmount(amount, "Budget limit"));
        budget.setMonth(Validation.month(month)); budget.setYear(Validation.year(year));
        return repository.save(budget);
    }

    public void delete(User user, String id) { repository.deleteByIdAndUserId(id, user.getId()); }
    public List<Budget> list(User user) { return repository.findByUserId(user.getId()); }

    private void validateCategory(Category category) {
        if (category.isIncomeCategory() && category != Category.OTHER)
            throw new ValidationException("Budgets are intended for expense categories.");
    }
}
