package com.finances.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;

import com.finances.core.domain.Transaction;
import com.finances.core.domain.TransactionType;
import com.finances.core.domain.Wallet;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FileStorageTest {
  private FileStorage fileStorage;
  private static final String TEST_USER = "testUser";

  @BeforeEach
  void setUp() {
    fileStorage = new FileStorage();
  }

  @AfterEach
  void tearDown() {
    fileStorage.deleteWallet(TEST_USER);
    new File(TEST_USER + "_export.csv").delete();
    new File(TEST_USER + "_export.json").delete();
  }

  @Test
  void shouldSaveAndLoadWallet() throws IOException {
    Wallet wallet = new Wallet(TEST_USER);
    wallet.addTransaction(
        new Transaction(
            "1", TransactionType.INCOME, "Salary", 5000.0, LocalDateTime.now(), "Monthly"));
    wallet.setBudget("Food", 1000.0);

    fileStorage.saveWallet(wallet);
    Wallet loadedWallet = fileStorage.loadWallet(TEST_USER);

    assertEquals(TEST_USER, loadedWallet.getUserId());
    assertEquals(1, loadedWallet.getTransactions().size());
    assertEquals(1, loadedWallet.getAllBudgets().size());
    assertEquals(5000.0, loadedWallet.getTotalIncome());
  }

  @Test
  void shouldReturnNewWalletWhenFileDoesNotExist() throws IOException {
    Wallet wallet = fileStorage.loadWallet("nonexistent");

    assertNotNull(wallet);
    assertEquals("nonexistent", wallet.getUserId());
    assertTrue(wallet.getTransactions().isEmpty());
  }

  @Test
  void shouldCheckIfWalletExists() throws IOException {
    Wallet wallet = new Wallet(TEST_USER);
    fileStorage.saveWallet(wallet);

    assertTrue(fileStorage.walletExists(TEST_USER));
    assertFalse(fileStorage.walletExists("nonexistent"));
  }

  @Test
  void shouldDeleteWallet() throws IOException {
    Wallet wallet = new Wallet(TEST_USER);
    fileStorage.saveWallet(wallet);

    boolean deleted = fileStorage.deleteWallet(TEST_USER);

    assertTrue(deleted);
    assertFalse(fileStorage.walletExists(TEST_USER));
  }

  @Test
  void shouldExportToCSV() throws IOException {
    Wallet wallet = new Wallet(TEST_USER);
    wallet.addTransaction(
        new Transaction(
            "1", TransactionType.INCOME, "Salary", 5000.0, LocalDateTime.now(), "Monthly"));
    wallet.addTransaction(
        new Transaction(
            "2", TransactionType.EXPENSE, "Food", 500.0, LocalDateTime.now(), "Groceries"));

    String filename = TEST_USER + "_export.csv";
    fileStorage.exportToCSV(wallet, filename);

    File file = new File(filename);
    assertTrue(file.exists());
  }

  @Test
  void shouldExportToJSON() throws IOException {
    Wallet wallet = new Wallet(TEST_USER);
    wallet.addTransaction(
        new Transaction(
            "1", TransactionType.INCOME, "Salary", 5000.0, LocalDateTime.now(), "Monthly"));

    String filename = TEST_USER + "_export.json";
    fileStorage.exportToJSON(wallet, filename);

    File file = new File(filename);
    assertTrue(file.exists());
  }
}

