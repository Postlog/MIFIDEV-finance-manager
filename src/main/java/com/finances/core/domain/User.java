package com.finances.core.domain;

import java.util.Objects;

/** Represents a user in the personal finance system. */
public class User {
  private final String username;
  private final String passwordHash;

  public User(String username, String passwordHash) {
    if (username == null || username.trim().isEmpty()) {
      throw new IllegalArgumentException("Username cannot be null or empty");
    }
    if (passwordHash == null || passwordHash.trim().isEmpty()) {
      throw new IllegalArgumentException("Password hash cannot be null or empty");
    }

    this.username = username.trim();
    this.passwordHash = passwordHash;
  }

  public String getUsername() {
    return username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public boolean verifyPassword(String passwordHash) {
    return this.passwordHash.equals(passwordHash);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return Objects.equals(username, user.username);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username);
  }

  @Override
  public String toString() {
    return "User{username='" + username + "'}";
  }
}

