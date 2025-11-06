package com.finances.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.finances.core.domain.Budget;
import com.finances.core.domain.Wallet;
import com.finances.core.repository.UserRepository;
import com.finances.core.repository.WalletRepository;
import com.finances.core.service.AuthService;
import com.finances.core.service.BudgetService;
import com.finances.core.service.TransactionService;
import com.finances.infrastructure.notification.NotificationService;
import com.finances.infrastructure.persistence.FileStorage;
import com.finances.infrastructure.persistence.InMemoryUserRepository;
import com.finances.infrastructure.persistence.InMemoryWalletRepository;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test that verifies the complete workflow of the finance management system. This test
 * covers: - User registration and authentication - Adding income and expenses - Setting and
 * managing budgets - Notifications for budget alerts - Data persistence (save/load) - Transfers
 * between users
 */
class FinanceSystemIntegrationTest {
  private AuthService authService;
  private TransactionService transactionService;
  private BudgetService budgetService;
  private NotificationService notificationService;
  private FileStorage fileStorage;
  private WalletRepository walletRepository;

  private static final String USER1 = "alice";
  private static final String USER2 = "bob";
  private static final String PASSWORD = "password123";

  @BeforeEach
  void setUp() {
    UserRepository userRepository = new InMemoryUserRepository();
    walletRepository = new InMemoryWalletRepository();

    authService = new AuthService(userRepository, walletRepository);
    transactionService = new TransactionService(walletRepository);
    budgetService = new BudgetService(walletRepository);
    notificationService = new NotificationService(budgetService, transactionService);
    fileStorage = new FileStorage();
  }

  @AfterEach
  void tearDown() {
    fileStorage.deleteWallet(USER1);
    fileStorage.deleteWallet(USER2);
    new File(USER1 + "_export.csv").delete();
    new File(USER1 + "_export.json").delete();
  }

  @Test
  void shouldCompleteFullUserWorkflow() throws IOException {
    // 1. Register and login
    assertTrue(authService.register(USER1, PASSWORD));
    assertTrue(authService.login(USER1, PASSWORD));
    assertTrue(authService.isAuthenticated());

    // 2. Add income
    transactionService.addIncome(USER1, "Зарплата", 20000.0, "Месячная зарплата");
    transactionService.addIncome(USER1, "Зарплата", 40000.0, "Премия");
    transactionService.addIncome(USER1, "Бонус", 3000.0, "");

    assertEquals(63000.0, transactionService.getTotalIncome(USER1));

    // 3. Add expenses
    transactionService.addExpense(USER1, "Еда", 300.0, "");
    transactionService.addExpense(USER1, "Еда", 500.0, "");
    transactionService.addExpense(USER1, "Развлечения", 3000.0, "");
    transactionService.addExpense(USER1, "Коммунальные услуги", 3000.0, "");
    transactionService.addExpense(USER1, "Такси", 1500.0, "");

    assertEquals(8300.0, transactionService.getTotalExpense(USER1));

    // 4. Set budgets
    budgetService.setBudget(USER1, "Еда", 4000.0);
    budgetService.setBudget(USER1, "Развлечения", 3000.0);
    budgetService.setBudget(USER1, "Коммунальные услуги", 2500.0);

    // 5. Verify budget calculations
    assertEquals(3200.0, budgetService.getRemainingBudget(USER1, "Еда"));
    assertEquals(0.0, budgetService.getRemainingBudget(USER1, "Развлечения"));
    assertEquals(-500.0, budgetService.getRemainingBudget(USER1, "Коммунальные услуги"));

    // 6. Verify budget exceeded notification
    assertTrue(budgetService.isBudgetExceeded(USER1, "Коммунальные услуги"));
    assertFalse(budgetService.isBudgetExceeded(USER1, "Еда"));

    // 7. Verify income by category
    Map<String, Double> incomeByCategory = transactionService.getIncomeByCategory(USER1);
    assertEquals(60000.0, incomeByCategory.get("Зарплата"));
    assertEquals(3000.0, incomeByCategory.get("Бонус"));

    // 8. Verify expense by category
    Map<String, Double> expenseByCategory = transactionService.getExpenseByCategory(USER1);
    assertEquals(800.0, expenseByCategory.get("Еда"));
    assertEquals(3000.0, expenseByCategory.get("Развлечения"));
    assertEquals(3000.0, expenseByCategory.get("Коммунальные услуги"));

    // 9. Verify balance
    assertEquals(54700.0, transactionService.getBalance(USER1));

    // 10. Test notifications
    List<String> notifications = notificationService.getNotifications(USER1);
    assertFalse(notifications.isEmpty());
    boolean hasBudgetExceededNotification =
        notifications.stream().anyMatch(n -> n.contains("Коммунальные услуги"));
    assertTrue(hasBudgetExceededNotification);

    // 11. Save wallet to file
    Wallet wallet = walletRepository.findByUserId(USER1).orElseThrow();
    fileStorage.saveWallet(wallet);
    assertTrue(fileStorage.walletExists(USER1));

    // 12. Logout and login again
    authService.logout();
    assertFalse(authService.isAuthenticated());

    assertTrue(authService.login(USER1, PASSWORD));

    // 13. Load wallet from file
    Wallet loadedWallet = fileStorage.loadWallet(USER1);
    walletRepository.save(loadedWallet);

    assertEquals(63000.0, transactionService.getTotalIncome(USER1));
    assertEquals(8300.0, transactionService.getTotalExpense(USER1));
    assertEquals(54700.0, transactionService.getBalance(USER1));

    // 14. Verify budgets persisted
    Map<String, Budget> budgets = budgetService.getAllBudgets(USER1);
    assertEquals(3, budgets.size());
    assertTrue(budgets.containsKey("Еда"));
    assertTrue(budgets.containsKey("Развлечения"));
    assertTrue(budgets.containsKey("Коммунальные услуги"));
  }

  @Test
  void shouldTransferBetweenUsers() {
    // Setup two users
    authService.register(USER1, PASSWORD);
    authService.register(USER2, PASSWORD);

    authService.login(USER1, PASSWORD);
    transactionService.addIncome(USER1, "Зарплата", 50000.0, "");

    authService.logout();
    authService.login(USER2, PASSWORD);

    // Transfer from USER1 to USER2
    transactionService.transfer(USER1, USER2, 5000.0, "Подарок");

    assertEquals(45000.0, transactionService.getBalance(USER1));
    assertEquals(5000.0, transactionService.getBalance(USER2));

    // Verify transactions (USER1: 1 income + 1 expense, USER2: 1 income)
    assertEquals(2, transactionService.getTransactions(USER1).size());
    assertEquals(1, transactionService.getTransactions(USER2).size());
  }

  @Test
  void shouldExportData() throws IOException {
    authService.register(USER1, PASSWORD);
    authService.login(USER1, PASSWORD);

    transactionService.addIncome(USER1, "Зарплата", 50000.0, "");
    transactionService.addExpense(USER1, "Еда", 5000.0, "");

    Wallet wallet = walletRepository.findByUserId(USER1).orElseThrow();

    // Export to CSV
    String csvFilename = USER1 + "_export.csv";
    fileStorage.exportToCSV(wallet, csvFilename);
    assertTrue(new File(csvFilename).exists());

    // Export to JSON
    String jsonFilename = USER1 + "_export.json";
    fileStorage.exportToJSON(wallet, jsonFilename);
    assertTrue(new File(jsonFilename).exists());
  }

  @Test
  void shouldHandleMultipleBudgetCategories() {
    authService.register(USER1, PASSWORD);
    authService.login(USER1, PASSWORD);

    // Set multiple budgets
    budgetService.setBudget(USER1, "Еда", 10000.0);
    budgetService.setBudget(USER1, "Транспорт", 5000.0);
    budgetService.setBudget(USER1, "Развлечения", 8000.0);

    // Add expenses for each category
    transactionService.addExpense(USER1, "Еда", 8500.0, "");
    transactionService.addExpense(USER1, "Транспорт", 4000.0, "");
    transactionService.addExpense(USER1, "Развлечения", 8500.0, "");

    // Verify usage percentages
    assertEquals(85.0, budgetService.getBudgetUsagePercentage(USER1, "Еда"));
    assertEquals(80.0, budgetService.getBudgetUsagePercentage(USER1, "Транспорт"));
    assertTrue(budgetService.getBudgetUsagePercentage(USER1, "Развлечения") > 100);

    // Verify notifications
    List<String> notifications = notificationService.getBudgetNotifications(USER1);
    assertEquals(3, notifications.size()); // All categories should have warnings
  }

  @Test
  void shouldFilterTransactionsByCategories() {
    authService.register(USER1, PASSWORD);
    authService.login(USER1, PASSWORD);

    transactionService.addIncome(USER1, "Зарплата", 50000.0, "");
    transactionService.addIncome(USER1, "Фриланс", 10000.0, "");
    transactionService.addIncome(USER1, "Бонус", 5000.0, "");

    transactionService.addExpense(USER1, "Еда", 5000.0, "");
    transactionService.addExpense(USER1, "Транспорт", 2000.0, "");
    transactionService.addExpense(USER1, "Развлечения", 3000.0, "");

    // Filter income by specific categories
    List<String> incomeCategories = List.of("Зарплата", "Фриланс");
    double filteredIncome = transactionService.getIncomeForCategories(USER1, incomeCategories);
    assertEquals(60000.0, filteredIncome);

    // Filter expenses by specific categories
    List<String> expenseCategories = List.of("Еда", "Транспорт");
    double filteredExpense = transactionService.getExpenseForCategories(USER1, expenseCategories);
    assertEquals(7000.0, filteredExpense);
  }
}

