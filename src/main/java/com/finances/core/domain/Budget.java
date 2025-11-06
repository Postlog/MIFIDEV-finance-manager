package com.finances.core.domain;

import java.util.Objects;

/** Represents a budget limit for a specific expense category. */
public class Budget {
  private final String category;
  private double limit;

  public Budget(String category, double limit) {
    if (category == null || category.trim().isEmpty()) {
      throw new IllegalArgumentException("Category cannot be null or empty");
    }
    if (limit < 0) {
      throw new IllegalArgumentException("Budget limit cannot be negative");
    }

    this.category = category.trim();
    this.limit = limit;
  }

  public String getCategory() {
    return category;
  }

  public double getLimit() {
    return limit;
  }

  public void setLimit(double limit) {
    if (limit < 0) {
      throw new IllegalArgumentException("Budget limit cannot be negative");
    }
    this.limit = limit;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Budget budget = (Budget) o;
    return Objects.equals(category, budget.category);
  }

  @Override
  public int hashCode() {
    return Objects.hash(category);
  }

  @Override
  public String toString() {
    return String.format("%s: %.2f", category, limit);
  }
}

