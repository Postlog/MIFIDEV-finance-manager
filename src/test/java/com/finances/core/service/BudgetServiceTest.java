package com.finances.core.service;

import static org.junit.jupiter.api.Assertions.*;

import com.finances.core.domain.Budget;
import com.finances.core.domain.Wallet;
import com.finances.infrastructure.persistence.InMemoryWalletRepository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BudgetServiceTest {
  private BudgetService budgetService;
  private TransactionService transactionService;
  private InMemoryWalletRepository walletRepository;
  private static final String USER_ID = "testUser";

  @BeforeEach
  void setUp() {
    walletRepository = new InMemoryWalletRepository();
    budgetService = new BudgetService(walletRepository);
    transactionService = new TransactionService(walletRepository);

    // Create wallet for test user
    Wallet wallet = new Wallet(USER_ID);
    walletRepository.save(wallet);
  }

  @Test
  void shouldSetBudget() {
    budgetService.setBudget(USER_ID, "Food", 1000.0);

    Budget budget = budgetService.getBudget(USER_ID, "Food");
    assertNotNull(budget);
    assertEquals("Food", budget.getCategory());
    assertEquals(1000.0, budget.getLimit());
  }

  @Test
  void shouldUpdateExistingBudget() {
    budgetService.setBudget(USER_ID, "Food", 1000.0);
    budgetService.setBudget(USER_ID, "Food", 1500.0);

    Budget budget = budgetService.getBudget(USER_ID, "Food");
    assertEquals(1500.0, budget.getLimit());
  }

  @Test
  void shouldGetAllBudgets() {
    budgetService.setBudget(USER_ID, "Food", 1000.0);
    budgetService.setBudget(USER_ID, "Transport", 500.0);

    Map<String, Budget> budgets = budgetService.getAllBudgets(USER_ID);
    assertEquals(2, budgets.size());
    assertTrue(budgets.containsKey("Food"));
    assertTrue(budgets.containsKey("Transport"));
  }

  @Test
  void shouldRemoveBudget() {
    budgetService.setBudget(USER_ID, "Food", 1000.0);
    budgetService.removeBudget(USER_ID, "Food");

    Budget budget = budgetService.getBudget(USER_ID, "Food");
    assertNull(budget);
  }

  @Test
  void shouldCalculateRemainingBudget() {
    budgetService.setBudget(USER_ID, "Food", 1000.0);
    transactionService.addExpense(USER_ID, "Food", 300.0, "");

    double remaining = budgetService.getRemainingBudget(USER_ID, "Food");
    assertEquals(700.0, remaining);
  }

  @Test
  void shouldDetectBudgetExceeded() {
    budgetService.setBudget(USER_ID, "Food", 1000.0);
    transactionService.addExpense(USER_ID, "Food", 1200.0, "");

    assertTrue(budgetService.isBudgetExceeded(USER_ID, "Food"));
  }

  @Test
  void shouldDetectBudgetNotExceeded() {
    budgetService.setBudget(USER_ID, "Food", 1000.0);
    transactionService.addExpense(USER_ID, "Food", 500.0, "");

    assertFalse(budgetService.isBudgetExceeded(USER_ID, "Food"));
  }

  @Test
  void shouldCalculateBudgetUsagePercentage() {
    budgetService.setBudget(USER_ID, "Food", 1000.0);
    transactionService.addExpense(USER_ID, "Food", 250.0, "");

    double percentage = budgetService.getBudgetUsagePercentage(USER_ID, "Food");
    assertEquals(25.0, percentage);
  }

  @Test
  void shouldReturnZeroPercentageWhenBudgetNotSet() {
    double percentage = budgetService.getBudgetUsagePercentage(USER_ID, "Food");
    assertEquals(0.0, percentage);
  }

  @Test
  void shouldReturnPercentageOver100WhenExceeded() {
    budgetService.setBudget(USER_ID, "Food", 1000.0);
    transactionService.addExpense(USER_ID, "Food", 1500.0, "");

    double percentage = budgetService.getBudgetUsagePercentage(USER_ID, "Food");
    assertEquals(150.0, percentage);
  }
}

