package com.financemanager.model;

public enum Category {
    SALARY,
    FREELANCE,
    INVESTMENT,
    FOOD,
    TRANSPORT,
    HOUSING,
    UTILITIES,
    HEALTH,
    EDUCATION,
    ENTERTAINMENT,
    SHOPPING,
    SAVINGS,
    OTHER;

    public boolean isIncomeCategory() {
        return this == SALARY || this == FREELANCE || this == INVESTMENT || this == OTHER;
    }
}
