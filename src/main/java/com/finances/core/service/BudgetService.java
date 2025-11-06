package com.finances.core.service;

import com.finances.core.domain.Budget;
import com.finances.core.domain.Wallet;
import com.finances.core.repository.WalletRepository;
import java.util.Map;

/** Service for managing budgets. */
public class BudgetService {
  private final WalletRepository walletRepository;

  public BudgetService(WalletRepository walletRepository) {
    this.walletRepository = walletRepository;
  }

  /**
   * Sets a budget for a category.
   *
   * @param userId the user ID
   * @param category the category
   * @param limit the budget limit
   */
  public void setBudget(String userId, String category, double limit) {
    Wallet wallet = getWallet(userId);
    wallet.setBudget(category, limit);
    walletRepository.save(wallet);
  }

  /**
   * Gets a budget for a category.
   *
   * @param userId the user ID
   * @param category the category
   * @return the budget or null if not found
   */
  public Budget getBudget(String userId, String category) {
    return getWallet(userId).getBudget(category);
  }

  /**
   * Gets all budgets for a user.
   *
   * @param userId the user ID
   * @return map of category to budget
   */
  public Map<String, Budget> getAllBudgets(String userId) {
    return getWallet(userId).getAllBudgets();
  }

  /**
   * Removes a budget for a category.
   *
   * @param userId the user ID
   * @param category the category
   */
  public void removeBudget(String userId, String category) {
    Wallet wallet = getWallet(userId);
    wallet.removeBudget(category);
    walletRepository.save(wallet);
  }

  /**
   * Gets the remaining budget for a category.
   *
   * @param userId the user ID
   * @param category the category
   * @return the remaining budget
   */
  public double getRemainingBudget(String userId, String category) {
    return getWallet(userId).getRemainingBudget(category);
  }

  /**
   * Checks if a budget is exceeded for a category.
   *
   * @param userId the user ID
   * @param category the category
   * @return true if the budget is exceeded, false otherwise
   */
  public boolean isBudgetExceeded(String userId, String category) {
    return getRemainingBudget(userId, category) < 0;
  }

  /**
   * Gets the percentage of budget used for a category.
   *
   * @param userId the user ID
   * @param category the category
   * @return the percentage (0-100+)
   */
  public double getBudgetUsagePercentage(String userId, String category) {
    Budget budget = getBudget(userId, category);
    if (budget == null || budget.getLimit() == 0) {
      return 0;
    }

    Wallet wallet = getWallet(userId);
    double spent = wallet.getExpenseForCategory(category);
    return (spent / budget.getLimit()) * 100;
  }

  private Wallet getWallet(String userId) {
    return walletRepository
        .findByUserId(userId)
        .orElseThrow(() -> new IllegalStateException("Wallet not found for user: " + userId));
  }
}

