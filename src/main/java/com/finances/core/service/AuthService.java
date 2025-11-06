package com.finances.core.service;

import com.finances.core.domain.User;
import com.finances.core.domain.Wallet;
import com.finances.core.repository.UserRepository;
import com.finances.core.repository.WalletRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

/** Service for handling user authentication and registration. */
public class AuthService {
  private final UserRepository userRepository;
  private final WalletRepository walletRepository;
  private User currentUser;

  public AuthService(UserRepository userRepository, WalletRepository walletRepository) {
    this.userRepository = userRepository;
    this.walletRepository = walletRepository;
  }

  /**
   * Registers a new user.
   *
   * @param username the username
   * @param password the password
   * @return true if registration was successful, false if username already exists
   */
  public boolean register(String username, String password) {
    if (username == null || username.trim().isEmpty()) {
      throw new IllegalArgumentException("Username cannot be empty");
    }
    if (password == null || password.isEmpty()) {
      throw new IllegalArgumentException("Password cannot be empty");
    }

    if (userRepository.existsByUsername(username)) {
      return false;
    }

    String passwordHash = hashPassword(password);
    User user = new User(username, passwordHash);
    userRepository.save(user);

    // Create wallet for the new user
    Wallet wallet = new Wallet(username);
    walletRepository.save(wallet);

    return true;
  }

  /**
   * Authenticates a user.
   *
   * @param username the username
   * @param password the password
   * @return true if authentication was successful, false otherwise
   */
  public boolean login(String username, String password) {
    if (username == null || password == null) {
      return false;
    }

    Optional<User> userOpt = userRepository.findByUsername(username);
    if (userOpt.isEmpty()) {
      return false;
    }

    User user = userOpt.get();
    String passwordHash = hashPassword(password);

    if (user.verifyPassword(passwordHash)) {
      currentUser = user;
      return true;
    }

    return false;
  }

  /** Logs out the current user. */
  public void logout() {
    currentUser = null;
  }

  /**
   * Gets the currently authenticated user.
   *
   * @return the current user or null if not authenticated
   */
  public User getCurrentUser() {
    return currentUser;
  }

  /**
   * Checks if a user is currently authenticated.
   *
   * @return true if a user is authenticated, false otherwise
   */
  public boolean isAuthenticated() {
    return currentUser != null;
  }

  /**
   * Hashes a password using SHA-256.
   *
   * @param password the password to hash
   * @return the hashed password
   */
  private String hashPassword(String password) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(password.getBytes());
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-256 algorithm not available", e);
    }
  }
}

