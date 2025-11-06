package com.finances.core.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/** Represents a financial transaction (income or expense). */
public class Transaction {
  private final String id;
  private final TransactionType type;
  private final String category;
  private final double amount;
  private final LocalDateTime timestamp;
  private final String description;

  public Transaction(
      String id,
      TransactionType type,
      String category,
      double amount,
      LocalDateTime timestamp,
      String description) {
    if (id == null || id.trim().isEmpty()) {
      throw new IllegalArgumentException("Transaction ID cannot be null or empty");
    }
    if (type == null) {
      throw new IllegalArgumentException("Transaction type cannot be null");
    }
    if (category == null || category.trim().isEmpty()) {
      throw new IllegalArgumentException("Category cannot be null or empty");
    }
    if (amount <= 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }
    if (timestamp == null) {
      throw new IllegalArgumentException("Timestamp cannot be null");
    }

    this.id = id;
    this.type = type;
    this.category = category.trim();
    this.amount = amount;
    this.timestamp = timestamp;
    this.description = description != null ? description.trim() : "";
  }

  public String getId() {
    return id;
  }

  public TransactionType getType() {
    return type;
  }

  public String getCategory() {
    return category;
  }

  public double getAmount() {
    return amount;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Transaction that = (Transaction) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return String.format(
        "%s: %s - %.2f (%s) [%s]",
        type, category, amount, timestamp.toLocalDate(), description);
  }
}

