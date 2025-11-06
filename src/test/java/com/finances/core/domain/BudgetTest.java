package com.finances.core.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BudgetTest {

  @Test
  void shouldCreateBudgetWithValidData() {
    Budget budget = new Budget("Food", 1000.0);

    assertEquals("Food", budget.getCategory());
    assertEquals(1000.0, budget.getLimit());
  }

  @Test
  void shouldThrowExceptionWhenCategoryIsNull() {
    assertThrows(IllegalArgumentException.class, () -> new Budget(null, 1000.0));
  }

  @Test
  void shouldThrowExceptionWhenCategoryIsEmpty() {
    assertThrows(IllegalArgumentException.class, () -> new Budget("", 1000.0));
  }

  @Test
  void shouldThrowExceptionWhenLimitIsNegative() {
    assertThrows(IllegalArgumentException.class, () -> new Budget("Food", -100.0));
  }

  @Test
  void shouldAllowZeroLimit() {
    Budget budget = new Budget("Food", 0.0);
    assertEquals(0.0, budget.getLimit());
  }

  @Test
  void shouldTrimCategory() {
    Budget budget = new Budget("  Food  ", 1000.0);
    assertEquals("Food", budget.getCategory());
  }

  @Test
  void shouldUpdateLimit() {
    Budget budget = new Budget("Food", 1000.0);
    budget.setLimit(1500.0);

    assertEquals(1500.0, budget.getLimit());
  }

  @Test
  void shouldThrowExceptionWhenUpdatingWithNegativeLimit() {
    Budget budget = new Budget("Food", 1000.0);
    assertThrows(IllegalArgumentException.class, () -> budget.setLimit(-100.0));
  }

  @Test
  void shouldBeEqualWhenCategoryIsEqual() {
    Budget budget1 = new Budget("Food", 1000.0);
    Budget budget2 = new Budget("Food", 2000.0);

    assertEquals(budget1, budget2);
  }
}

