package com.finances.core.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Represents a user's wallet containing transactions and budgets. */
public class Wallet {
  private final String userId;
  private final List<Transaction> transactions;
  private final Map<String, Budget> budgets;

  public Wallet(String userId) {
    if (userId == null || userId.trim().isEmpty()) {
      throw new IllegalArgumentException("User ID cannot be null or empty");
    }
    this.userId = userId;
    this.transactions = new ArrayList<>();
    this.budgets = new HashMap<>();
  }

  public String getUserId() {
    return userId;
  }

  public void addTransaction(Transaction transaction) {
    if (transaction == null) {
      throw new IllegalArgumentException("Transaction cannot be null");
    }
    transactions.add(transaction);
  }

  public List<Transaction> getTransactions() {
    return Collections.unmodifiableList(transactions);
  }

  public void setBudget(String category, double limit) {
    if (category == null || category.trim().isEmpty()) {
      throw new IllegalArgumentException("Category cannot be null or empty");
    }
    budgets.put(category.trim(), new Budget(category.trim(), limit));
  }

  public Budget getBudget(String category) {
    return budgets.get(category);
  }

  public Map<String, Budget> getAllBudgets() {
    return Collections.unmodifiableMap(budgets);
  }

  public void removeBudget(String category) {
    budgets.remove(category);
  }

  public double getTotalIncome() {
    return transactions.stream()
        .filter(t -> t.getType() == TransactionType.INCOME)
        .mapToDouble(Transaction::getAmount)
        .sum();
  }

  public double getTotalExpense() {
    return transactions.stream()
        .filter(t -> t.getType() == TransactionType.EXPENSE)
        .mapToDouble(Transaction::getAmount)
        .sum();
  }

  public double getBalance() {
    return getTotalIncome() - getTotalExpense();
  }

  public Map<String, Double> getIncomeByCategory() {
    return transactions.stream()
        .filter(t -> t.getType() == TransactionType.INCOME)
        .collect(
            Collectors.groupingBy(
                Transaction::getCategory, Collectors.summingDouble(Transaction::getAmount)));
  }

  public Map<String, Double> getExpenseByCategory() {
    return transactions.stream()
        .filter(t -> t.getType() == TransactionType.EXPENSE)
        .collect(
            Collectors.groupingBy(
                Transaction::getCategory, Collectors.summingDouble(Transaction::getAmount)));
  }

  public double getExpenseForCategory(String category) {
    return transactions.stream()
        .filter(t -> t.getType() == TransactionType.EXPENSE)
        .filter(t -> t.getCategory().equals(category))
        .mapToDouble(Transaction::getAmount)
        .sum();
  }

  public double getRemainingBudget(String category) {
    Budget budget = budgets.get(category);
    if (budget == null) {
      return 0;
    }
    double spent = getExpenseForCategory(category);
    return budget.getLimit() - spent;
  }

  public void clear() {
    transactions.clear();
    budgets.clear();
  }
}

