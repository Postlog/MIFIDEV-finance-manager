package com.finances.core.service;

import com.finances.core.domain.Transaction;
import com.finances.core.domain.TransactionType;
import com.finances.core.domain.Wallet;
import com.finances.core.repository.WalletRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Service for managing transactions. */
public class TransactionService {
  private final WalletRepository walletRepository;

  public TransactionService(WalletRepository walletRepository) {
    this.walletRepository = walletRepository;
  }

  /**
   * Adds an income transaction.
   *
   * @param userId the user ID
   * @param category the income category
   * @param amount the amount
   * @param description optional description
   * @return the created transaction
   */
  public Transaction addIncome(String userId, String category, double amount, String description) {
    Wallet wallet = getWallet(userId);
    Transaction transaction =
        new Transaction(
            generateId(), TransactionType.INCOME, category, amount, LocalDateTime.now(), description);
    wallet.addTransaction(transaction);
    walletRepository.save(wallet);
    return transaction;
  }

  /**
   * Adds an expense transaction.
   *
   * @param userId the user ID
   * @param category the expense category
   * @param amount the amount
   * @param description optional description
   * @return the created transaction
   */
  public Transaction addExpense(String userId, String category, double amount, String description) {
    Wallet wallet = getWallet(userId);
    Transaction transaction =
        new Transaction(
            generateId(),
            TransactionType.EXPENSE,
            category,
            amount,
            LocalDateTime.now(),
            description);
    wallet.addTransaction(transaction);
    walletRepository.save(wallet);
    return transaction;
  }

  /**
   * Gets all transactions for a user.
   *
   * @param userId the user ID
   * @return list of transactions
   */
  public List<Transaction> getTransactions(String userId) {
    return getWallet(userId).getTransactions();
  }

  /**
   * Gets total income for a user.
   *
   * @param userId the user ID
   * @return the total income
   */
  public double getTotalIncome(String userId) {
    return getWallet(userId).getTotalIncome();
  }

  /**
   * Gets total expenses for a user.
   *
   * @param userId the user ID
   * @return the total expenses
   */
  public double getTotalExpense(String userId) {
    return getWallet(userId).getTotalExpense();
  }

  /**
   * Gets income grouped by category.
   *
   * @param userId the user ID
   * @return map of category to total income
   */
  public Map<String, Double> getIncomeByCategory(String userId) {
    return getWallet(userId).getIncomeByCategory();
  }

  /**
   * Gets expenses grouped by category.
   *
   * @param userId the user ID
   * @return map of category to total expenses
   */
  public Map<String, Double> getExpenseByCategory(String userId) {
    return getWallet(userId).getExpenseByCategory();
  }

  /**
   * Gets total income for specific categories.
   *
   * @param userId the user ID
   * @param categories list of categories
   * @return the total income for specified categories
   */
  public double getIncomeForCategories(String userId, List<String> categories) {
    Map<String, Double> incomeByCategory = getIncomeByCategory(userId);
    return categories.stream()
        .filter(incomeByCategory::containsKey)
        .mapToDouble(incomeByCategory::get)
        .sum();
  }

  /**
   * Gets total expenses for specific categories.
   *
   * @param userId the user ID
   * @param categories list of categories
   * @return the total expenses for specified categories
   */
  public double getExpenseForCategories(String userId, List<String> categories) {
    Map<String, Double> expenseByCategory = getExpenseByCategory(userId);
    return categories.stream()
        .filter(expenseByCategory::containsKey)
        .mapToDouble(expenseByCategory::get)
        .sum();
  }

  /**
   * Gets transactions for specific categories.
   *
   * @param userId the user ID
   * @param categories list of categories
   * @return list of transactions
   */
  public List<Transaction> getTransactionsForCategories(String userId, List<String> categories) {
    return getTransactions(userId).stream()
        .filter(t -> categories.contains(t.getCategory()))
        .collect(Collectors.toList());
  }

  /**
   * Gets the current balance for a user.
   *
   * @param userId the user ID
   * @return the balance
   */
  public double getBalance(String userId) {
    return getWallet(userId).getBalance();
  }

  /**
   * Transfers money from one user to another.
   *
   * @param fromUserId the sender's user ID
   * @param toUserId the receiver's user ID
   * @param amount the amount to transfer
   * @param description optional description
   */
  public void transfer(String fromUserId, String toUserId, double amount, String description) {
    if (amount <= 0) {
      throw new IllegalArgumentException("Transfer amount must be positive");
    }

    Wallet fromWallet = getWallet(fromUserId);
    if (fromWallet.getBalance() < amount) {
      throw new IllegalStateException("Insufficient balance for transfer");
    }

    // Check if receiver exists
    Wallet toWallet = getWallet(toUserId);

    // Add expense for sender
    String transferDesc = "Transfer to " + toUserId + (description != null ? ": " + description : "");
    addExpense(fromUserId, "Transfer", amount, transferDesc);

    // Add income for receiver
    String receiveDesc =
        "Transfer from " + fromUserId + (description != null ? ": " + description : "");
    addIncome(toUserId, "Transfer", amount, receiveDesc);
  }

  private Wallet getWallet(String userId) {
    return walletRepository
        .findByUserId(userId)
        .orElseThrow(() -> new IllegalStateException("Wallet not found for user: " + userId));
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }
}

