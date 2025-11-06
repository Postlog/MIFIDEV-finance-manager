package com.finances.infrastructure.notification;

import static org.junit.jupiter.api.Assertions.*;

import com.finances.core.domain.Wallet;
import com.finances.core.service.BudgetService;
import com.finances.core.service.TransactionService;
import com.finances.infrastructure.persistence.InMemoryWalletRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationServiceTest {
  private NotificationService notificationService;
  private BudgetService budgetService;
  private TransactionService transactionService;
  private static final String USER_ID = "testUser";

  @BeforeEach
  void setUp() {
    InMemoryWalletRepository walletRepository = new InMemoryWalletRepository();
    budgetService = new BudgetService(walletRepository);
    transactionService = new TransactionService(walletRepository);
    notificationService = new NotificationService(budgetService, transactionService);

    // Create wallet for test user
    Wallet wallet = new Wallet(USER_ID);
    walletRepository.save(wallet);
  }

  @Test
  void shouldGenerateNotificationWhenBudgetExceeded() {
    budgetService.setBudget(USER_ID, "Food", 1000.0);
    transactionService.addExpense(USER_ID, "Food", 1200.0, "");

    List<String> notifications = notificationService.getBudgetNotifications(USER_ID);
    assertFalse(notifications.isEmpty());
    assertTrue(notifications.get(0).contains("ПРЕВЫШЕНИЕ БЮДЖЕТА"));
  }

  @Test
  void shouldGenerateWarningWhenBudgetUsageOver80Percent() {
    budgetService.setBudget(USER_ID, "Food", 1000.0);
    transactionService.addExpense(USER_ID, "Food", 850.0, "");

    List<String> notifications = notificationService.getBudgetNotifications(USER_ID);
    assertFalse(notifications.isEmpty());
    assertTrue(notifications.get(0).contains("ПРЕДУПРЕЖДЕНИЕ"));
  }

  @Test
  void shouldNotGenerateNotificationWhenBudgetUsageBelow80Percent() {
    budgetService.setBudget(USER_ID, "Food", 1000.0);
    transactionService.addExpense(USER_ID, "Food", 500.0, "");

    List<String> notifications = notificationService.getBudgetNotifications(USER_ID);
    assertTrue(notifications.isEmpty());
  }

  @Test
  void shouldGenerateNotificationWhenExpensesExceedIncome() {
    transactionService.addIncome(USER_ID, "Salary", 5000.0, "");
    transactionService.addExpense(USER_ID, "Expenses", 6000.0, "");

    List<String> notifications = notificationService.getNotifications(USER_ID);
    boolean hasExpenseWarning =
        notifications.stream().anyMatch(n -> n.contains("Расходы") && n.contains("превышают доходы"));
    assertTrue(hasExpenseWarning);
  }

  @Test
  void shouldGenerateNotificationForNegativeBalance() {
    transactionService.addIncome(USER_ID, "Salary", 5000.0, "");
    transactionService.addExpense(USER_ID, "Expenses", 6000.0, "");

    List<String> notifications = notificationService.getNotifications(USER_ID);
    boolean hasBalanceWarning =
        notifications.stream().anyMatch(n -> n.contains("Отрицательный баланс"));
    assertTrue(hasBalanceWarning);
  }

  @Test
  void shouldNotGenerateNotificationsWhenEverythingIsOk() {
    budgetService.setBudget(USER_ID, "Food", 1000.0);
    transactionService.addIncome(USER_ID, "Salary", 5000.0, "");
    transactionService.addExpense(USER_ID, "Food", 500.0, "");

    List<String> notifications = notificationService.getNotifications(USER_ID);
    assertTrue(notifications.isEmpty());
  }
}

