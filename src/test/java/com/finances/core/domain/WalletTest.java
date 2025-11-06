package com.finances.core.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WalletTest {
  private Wallet wallet;

  @BeforeEach
  void setUp() {
    wallet = new Wallet("testUser");
  }

  @Test
  void shouldCreateWalletWithUserId() {
    assertEquals("testUser", wallet.getUserId());
    assertTrue(wallet.getTransactions().isEmpty());
    assertTrue(wallet.getAllBudgets().isEmpty());
  }

  @Test
  void shouldThrowExceptionWhenUserIdIsNull() {
    assertThrows(IllegalArgumentException.class, () -> new Wallet(null));
  }

  @Test
  void shouldAddTransaction() {
    Transaction transaction =
        new Transaction("1", TransactionType.INCOME, "Salary", 5000.0, LocalDateTime.now(), "");
    wallet.addTransaction(transaction);

    assertEquals(1, wallet.getTransactions().size());
    assertTrue(wallet.getTransactions().contains(transaction));
  }

  @Test
  void shouldCalculateTotalIncome() {
    wallet.addTransaction(
        new Transaction("1", TransactionType.INCOME, "Salary", 5000.0, LocalDateTime.now(), ""));
    wallet.addTransaction(
        new Transaction("2", TransactionType.INCOME, "Bonus", 1000.0, LocalDateTime.now(), ""));
    wallet.addTransaction(
        new Transaction("3", TransactionType.EXPENSE, "Food", 500.0, LocalDateTime.now(), ""));

    assertEquals(6000.0, wallet.getTotalIncome());
  }

  @Test
  void shouldCalculateTotalExpense() {
    wallet.addTransaction(
        new Transaction("1", TransactionType.EXPENSE, "Food", 500.0, LocalDateTime.now(), ""));
    wallet.addTransaction(
        new Transaction(
            "2", TransactionType.EXPENSE, "Transport", 200.0, LocalDateTime.now(), ""));
    wallet.addTransaction(
        new Transaction("3", TransactionType.INCOME, "Salary", 5000.0, LocalDateTime.now(), ""));

    assertEquals(700.0, wallet.getTotalExpense());
  }

  @Test
  void shouldCalculateBalance() {
    wallet.addTransaction(
        new Transaction("1", TransactionType.INCOME, "Salary", 5000.0, LocalDateTime.now(), ""));
    wallet.addTransaction(
        new Transaction("2", TransactionType.EXPENSE, "Food", 500.0, LocalDateTime.now(), ""));

    assertEquals(4500.0, wallet.getBalance());
  }

  @Test
  void shouldGroupIncomeByCategory() {
    wallet.addTransaction(
        new Transaction("1", TransactionType.INCOME, "Salary", 5000.0, LocalDateTime.now(), ""));
    wallet.addTransaction(
        new Transaction("2", TransactionType.INCOME, "Salary", 5000.0, LocalDateTime.now(), ""));
    wallet.addTransaction(
        new Transaction("3", TransactionType.INCOME, "Bonus", 1000.0, LocalDateTime.now(), ""));

    Map<String, Double> incomeByCategory = wallet.getIncomeByCategory();
    assertEquals(10000.0, incomeByCategory.get("Salary"));
    assertEquals(1000.0, incomeByCategory.get("Bonus"));
  }

  @Test
  void shouldGroupExpenseByCategory() {
    wallet.addTransaction(
        new Transaction("1", TransactionType.EXPENSE, "Food", 300.0, LocalDateTime.now(), ""));
    wallet.addTransaction(
        new Transaction("2", TransactionType.EXPENSE, "Food", 200.0, LocalDateTime.now(), ""));
    wallet.addTransaction(
        new Transaction(
            "3", TransactionType.EXPENSE, "Transport", 500.0, LocalDateTime.now(), ""));

    Map<String, Double> expenseByCategory = wallet.getExpenseByCategory();
    assertEquals(500.0, expenseByCategory.get("Food"));
    assertEquals(500.0, expenseByCategory.get("Transport"));
  }

  @Test
  void shouldSetAndGetBudget() {
    wallet.setBudget("Food", 1000.0);

    Budget budget = wallet.getBudget("Food");
    assertNotNull(budget);
    assertEquals("Food", budget.getCategory());
    assertEquals(1000.0, budget.getLimit());
  }

  @Test
  void shouldCalculateRemainingBudget() {
    wallet.setBudget("Food", 1000.0);
    wallet.addTransaction(
        new Transaction("1", TransactionType.EXPENSE, "Food", 300.0, LocalDateTime.now(), ""));
    wallet.addTransaction(
        new Transaction("2", TransactionType.EXPENSE, "Food", 200.0, LocalDateTime.now(), ""));

    assertEquals(500.0, wallet.getRemainingBudget("Food"));
  }

  @Test
  void shouldReturnZeroRemainingBudgetWhenBudgetNotSet() {
    assertEquals(0, wallet.getRemainingBudget("Food"));
  }

  @Test
  void shouldRemoveBudget() {
    wallet.setBudget("Food", 1000.0);
    wallet.removeBudget("Food");

    assertNull(wallet.getBudget("Food"));
  }

  @Test
  void shouldClearAllData() {
    wallet.addTransaction(
        new Transaction("1", TransactionType.INCOME, "Salary", 5000.0, LocalDateTime.now(), ""));
    wallet.setBudget("Food", 1000.0);

    wallet.clear();

    assertTrue(wallet.getTransactions().isEmpty());
    assertTrue(wallet.getAllBudgets().isEmpty());
  }
}

