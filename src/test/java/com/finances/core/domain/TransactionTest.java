package com.finances.core.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class TransactionTest {

  @Test
  void shouldCreateTransactionWithValidData() {
    LocalDateTime now = LocalDateTime.now();
    Transaction transaction =
        new Transaction("1", TransactionType.INCOME, "Salary", 5000.0, now, "Monthly salary");

    assertEquals("1", transaction.getId());
    assertEquals(TransactionType.INCOME, transaction.getType());
    assertEquals("Salary", transaction.getCategory());
    assertEquals(5000.0, transaction.getAmount());
    assertEquals(now, transaction.getTimestamp());
    assertEquals("Monthly salary", transaction.getDescription());
  }

  @Test
  void shouldThrowExceptionWhenIdIsNull() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Transaction(
                null, TransactionType.INCOME, "Salary", 5000.0, LocalDateTime.now(), ""));
  }

  @Test
  void shouldThrowExceptionWhenCategoryIsEmpty() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Transaction("1", TransactionType.INCOME, "", 5000.0, LocalDateTime.now(), ""));
  }

  @Test
  void shouldThrowExceptionWhenAmountIsNegative() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Transaction(
                "1", TransactionType.INCOME, "Salary", -100.0, LocalDateTime.now(), ""));
  }

  @Test
  void shouldThrowExceptionWhenAmountIsZero() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Transaction("1", TransactionType.INCOME, "Salary", 0.0, LocalDateTime.now(), ""));
  }

  @Test
  void shouldTrimCategoryAndDescription() {
    Transaction transaction =
        new Transaction(
            "1", TransactionType.INCOME, "  Salary  ", 5000.0, LocalDateTime.now(), "  Test  ");

    assertEquals("Salary", transaction.getCategory());
    assertEquals("Test", transaction.getDescription());
  }

  @Test
  void shouldHandleNullDescription() {
    Transaction transaction =
        new Transaction("1", TransactionType.INCOME, "Salary", 5000.0, LocalDateTime.now(), null);

    assertEquals("", transaction.getDescription());
  }

  @Test
  void shouldBeEqualWhenIdIsEqual() {
    LocalDateTime now = LocalDateTime.now();
    Transaction t1 = new Transaction("1", TransactionType.INCOME, "Salary", 5000.0, now, "");
    Transaction t2 = new Transaction("1", TransactionType.EXPENSE, "Food", 100.0, now, "");

    assertEquals(t1, t2);
  }
}
