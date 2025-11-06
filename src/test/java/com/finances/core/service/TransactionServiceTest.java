package com.finances.core.service;

import static org.junit.jupiter.api.Assertions.*;

import com.finances.core.domain.Transaction;
import com.finances.core.domain.TransactionType;
import com.finances.core.domain.Wallet;
import com.finances.infrastructure.persistence.InMemoryWalletRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionServiceTest {
  private TransactionService transactionService;
  private InMemoryWalletRepository walletRepository;
  private static final String USER_ID = "testUser";

  @BeforeEach
  void setUp() {
    walletRepository = new InMemoryWalletRepository();
    transactionService = new TransactionService(walletRepository);

    // Create wallet for test user
    Wallet wallet = new Wallet(USER_ID);
    walletRepository.save(wallet);
  }

  @Test
  void shouldAddIncome() {
    Transaction transaction = transactionService.addIncome(USER_ID, "Salary", 5000.0, "Monthly");

    assertNotNull(transaction);
    assertEquals(TransactionType.INCOME, transaction.getType());
    assertEquals("Salary", transaction.getCategory());
    assertEquals(5000.0, transaction.getAmount());
  }

  @Test
  void shouldAddExpense() {
    Transaction transaction = transactionService.addExpense(USER_ID, "Food", 500.0, "Groceries");

    assertNotNull(transaction);
    assertEquals(TransactionType.EXPENSE, transaction.getType());
    assertEquals("Food", transaction.getCategory());
    assertEquals(500.0, transaction.getAmount());
  }

  @Test
  void shouldGetAllTransactions() {
    transactionService.addIncome(USER_ID, "Salary", 5000.0, "");
    transactionService.addExpense(USER_ID, "Food", 500.0, "");

    List<Transaction> transactions = transactionService.getTransactions(USER_ID);
    assertEquals(2, transactions.size());
  }

  @Test
  void shouldCalculateTotalIncome() {
    transactionService.addIncome(USER_ID, "Salary", 5000.0, "");
    transactionService.addIncome(USER_ID, "Bonus", 1000.0, "");

    double totalIncome = transactionService.getTotalIncome(USER_ID);
    assertEquals(6000.0, totalIncome);
  }

  @Test
  void shouldCalculateTotalExpense() {
    transactionService.addExpense(USER_ID, "Food", 500.0, "");
    transactionService.addExpense(USER_ID, "Transport", 200.0, "");

    double totalExpense = transactionService.getTotalExpense(USER_ID);
    assertEquals(700.0, totalExpense);
  }

  @Test
  void shouldGetIncomeByCategory() {
    transactionService.addIncome(USER_ID, "Salary", 5000.0, "");
    transactionService.addIncome(USER_ID, "Salary", 5000.0, "");
    transactionService.addIncome(USER_ID, "Bonus", 1000.0, "");

    Map<String, Double> incomeByCategory = transactionService.getIncomeByCategory(USER_ID);
    assertEquals(10000.0, incomeByCategory.get("Salary"));
    assertEquals(1000.0, incomeByCategory.get("Bonus"));
  }

  @Test
  void shouldGetExpenseByCategory() {
    transactionService.addExpense(USER_ID, "Food", 300.0, "");
    transactionService.addExpense(USER_ID, "Food", 200.0, "");
    transactionService.addExpense(USER_ID, "Transport", 500.0, "");

    Map<String, Double> expenseByCategory = transactionService.getExpenseByCategory(USER_ID);
    assertEquals(500.0, expenseByCategory.get("Food"));
    assertEquals(500.0, expenseByCategory.get("Transport"));
  }

  @Test
  void shouldGetIncomeForSpecificCategories() {
    transactionService.addIncome(USER_ID, "Salary", 5000.0, "");
    transactionService.addIncome(USER_ID, "Bonus", 1000.0, "");
    transactionService.addIncome(USER_ID, "Freelance", 2000.0, "");

    List<String> categories = Arrays.asList("Salary", "Bonus");
    double income = transactionService.getIncomeForCategories(USER_ID, categories);

    assertEquals(6000.0, income);
  }

  @Test
  void shouldGetExpenseForSpecificCategories() {
    transactionService.addExpense(USER_ID, "Food", 500.0, "");
    transactionService.addExpense(USER_ID, "Transport", 200.0, "");
    transactionService.addExpense(USER_ID, "Entertainment", 300.0, "");

    List<String> categories = Arrays.asList("Food", "Transport");
    double expense = transactionService.getExpenseForCategories(USER_ID, categories);

    assertEquals(700.0, expense);
  }

  @Test
  void shouldCalculateBalance() {
    transactionService.addIncome(USER_ID, "Salary", 5000.0, "");
    transactionService.addExpense(USER_ID, "Food", 500.0, "");

    double balance = transactionService.getBalance(USER_ID);
    assertEquals(4500.0, balance);
  }

  @Test
  void shouldTransferBetweenUsers() {
    String toUserId = "anotherUser";
    Wallet toWallet = new Wallet(toUserId);
    walletRepository.save(toWallet);

    transactionService.addIncome(USER_ID, "Salary", 5000.0, "");
    transactionService.transfer(USER_ID, toUserId, 1000.0, "Payment");

    assertEquals(4000.0, transactionService.getBalance(USER_ID));
    assertEquals(1000.0, transactionService.getBalance(toUserId));
  }

  @Test
  void shouldThrowExceptionWhenTransferAmountIsNegative() {
    String toUserId = "anotherUser";
    Wallet toWallet = new Wallet(toUserId);
    walletRepository.save(toWallet);

    assertThrows(
        IllegalArgumentException.class,
        () -> transactionService.transfer(USER_ID, toUserId, -100.0, ""));
  }

  @Test
  void shouldThrowExceptionWhenInsufficientBalance() {
    String toUserId = "anotherUser";
    Wallet toWallet = new Wallet(toUserId);
    walletRepository.save(toWallet);

    transactionService.addIncome(USER_ID, "Salary", 1000.0, "");

    assertThrows(
        IllegalStateException.class,
        () -> transactionService.transfer(USER_ID, toUserId, 2000.0, ""));
  }
}

