package com.finances.core.service;

import static org.junit.jupiter.api.Assertions.*;

import com.finances.core.domain.User;
import com.finances.infrastructure.persistence.InMemoryUserRepository;
import com.finances.infrastructure.persistence.InMemoryWalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthServiceTest {
  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService = new AuthService(new InMemoryUserRepository(), new InMemoryWalletRepository());
  }

  @Test
  void shouldRegisterNewUser() {
    boolean result = authService.register("testUser", "password123");

    assertTrue(result);
  }

  @Test
  void shouldNotRegisterDuplicateUser() {
    authService.register("testUser", "password123");
    boolean result = authService.register("testUser", "password456");

    assertFalse(result);
  }

  @Test
  void shouldThrowExceptionWhenRegisteringWithEmptyUsername() {
    assertThrows(IllegalArgumentException.class, () -> authService.register("", "password123"));
  }

  @Test
  void shouldThrowExceptionWhenRegisteringWithEmptyPassword() {
    assertThrows(IllegalArgumentException.class, () -> authService.register("testUser", ""));
  }

  @Test
  void shouldLoginWithCorrectCredentials() {
    authService.register("testUser", "password123");
    boolean result = authService.login("testUser", "password123");

    assertTrue(result);
    assertTrue(authService.isAuthenticated());
  }

  @Test
  void shouldNotLoginWithIncorrectPassword() {
    authService.register("testUser", "password123");
    boolean result = authService.login("testUser", "wrongPassword");

    assertFalse(result);
    assertFalse(authService.isAuthenticated());
  }

  @Test
  void shouldNotLoginWithNonexistentUser() {
    boolean result = authService.login("nonexistent", "password123");

    assertFalse(result);
    assertFalse(authService.isAuthenticated());
  }

  @Test
  void shouldGetCurrentUserAfterLogin() {
    authService.register("testUser", "password123");
    authService.login("testUser", "password123");

    User currentUser = authService.getCurrentUser();
    assertNotNull(currentUser);
    assertEquals("testUser", currentUser.getUsername());
  }

  @Test
  void shouldLogout() {
    authService.register("testUser", "password123");
    authService.login("testUser", "password123");
    authService.logout();

    assertFalse(authService.isAuthenticated());
    assertNull(authService.getCurrentUser());
  }
}

