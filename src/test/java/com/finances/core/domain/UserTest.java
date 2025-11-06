package com.finances.core.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void shouldCreateUserWithValidData() {
    User user = new User("testUser", "hashedPassword");

    assertEquals("testUser", user.getUsername());
    assertEquals("hashedPassword", user.getPasswordHash());
  }

  @Test
  void shouldThrowExceptionWhenUsernameIsNull() {
    assertThrows(IllegalArgumentException.class, () -> new User(null, "password"));
  }

  @Test
  void shouldThrowExceptionWhenUsernameIsEmpty() {
    assertThrows(IllegalArgumentException.class, () -> new User("", "password"));
  }

  @Test
  void shouldThrowExceptionWhenPasswordHashIsNull() {
    assertThrows(IllegalArgumentException.class, () -> new User("testUser", null));
  }

  @Test
  void shouldTrimUsername() {
    User user = new User("  testUser  ", "password");
    assertEquals("testUser", user.getUsername());
  }

  @Test
  void shouldVerifyCorrectPassword() {
    User user = new User("testUser", "hashedPassword");
    assertTrue(user.verifyPassword("hashedPassword"));
  }

  @Test
  void shouldNotVerifyIncorrectPassword() {
    User user = new User("testUser", "hashedPassword");
    assertFalse(user.verifyPassword("wrongPassword"));
  }

  @Test
  void shouldBeEqualWhenUsernameIsEqual() {
    User user1 = new User("testUser", "password1");
    User user2 = new User("testUser", "password2");

    assertEquals(user1, user2);
  }

  @Test
  void shouldNotBeEqualWhenUsernameIsDifferent() {
    User user1 = new User("testUser1", "password");
    User user2 = new User("testUser2", "password");

    assertNotEquals(user1, user2);
  }
}

