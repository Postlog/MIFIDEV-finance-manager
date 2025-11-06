package com.finances.infrastructure.notification;

import com.finances.core.domain.Budget;
import com.finances.core.service.BudgetService;
import com.finances.core.service.TransactionService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Service for generating user notifications about budgets and finances. */
public class NotificationService {
  private final BudgetService budgetService;
  private final TransactionService transactionService;

  public NotificationService(BudgetService budgetService, TransactionService transactionService) {
    this.budgetService = budgetService;
    this.transactionService = transactionService;
  }

  /**
   * Gets all notifications for a user.
   *
   * @param userId the user ID
   * @return list of notification messages
   */
  public List<String> getNotifications(String userId) {
    List<String> notifications = new ArrayList<>();

    // Check budget warnings
    notifications.addAll(getBudgetNotifications(userId));

    // Check if expenses exceed income
    double totalIncome = transactionService.getTotalIncome(userId);
    double totalExpense = transactionService.getTotalExpense(userId);
    if (totalExpense > totalIncome) {
      notifications.add(
          String.format(
              "⚠️  ВНИМАНИЕ: Расходы (%.2f) превышают доходы (%.2f) на %.2f",
              totalExpense, totalIncome, totalExpense - totalIncome));
    }

    // Check for zero or negative balance
    double balance = transactionService.getBalance(userId);
    if (balance <= 0) {
      notifications.add(String.format("⚠️  ВНИМАНИЕ: Отрицательный баланс: %.2f", balance));
    }

    return notifications;
  }

  /**
   * Gets budget-related notifications for a user.
   *
   * @param userId the user ID
   * @return list of budget notification messages
   */
  public List<String> getBudgetNotifications(String userId) {
    List<String> notifications = new ArrayList<>();
    Map<String, Budget> budgets = budgetService.getAllBudgets(userId);

    for (Map.Entry<String, Budget> entry : budgets.entrySet()) {
      String category = entry.getKey();
      double remaining = budgetService.getRemainingBudget(userId, category);
      double percentage = budgetService.getBudgetUsagePercentage(userId, category);

      if (remaining < 0) {
        notifications.add(
            String.format(
                "🚨 ПРЕВЫШЕНИЕ БЮДЖЕТА: Категория '%s' превышена на %.2f (%.0f%%)",
                category, Math.abs(remaining), percentage));
      } else if (percentage >= 80) {
        notifications.add(
            String.format(
                "⚠️  ПРЕДУПРЕЖДЕНИЕ: Категория '%s' израсходована на %.0f%% (осталось: %.2f)",
                category, percentage, remaining));
      }
    }

    return notifications;
  }

  /**
   * Checks and displays notifications after a transaction.
   *
   * @param userId the user ID
   * @param category the transaction category
   */
  public void checkAndNotifyAfterTransaction(String userId, String category) {
    Budget budget = budgetService.getBudget(userId, category);
    if (budget == null) {
      return;
    }

    double remaining = budgetService.getRemainingBudget(userId, category);
    double percentage = budgetService.getBudgetUsagePercentage(userId, category);

    if (remaining < 0) {
      System.out.println(
          String.format(
              "🚨 ВНИМАНИЕ: Бюджет категории '%s' превышен на %.2f!", category, Math.abs(remaining)));
    } else if (percentage >= 80) {
      System.out.println(
          String.format(
              "⚠️  Предупреждение: Использовано %.0f%% бюджета категории '%s' (осталось: %.2f)",
              percentage, category, remaining));
    }
  }
}

