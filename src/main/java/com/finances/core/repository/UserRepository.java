package com.finances.core.repository;

import com.finances.core.domain.User;
import java.util.Optional;

/** Repository interface for managing users. */
public interface UserRepository {
  /**
   * Saves a user to the repository.
   *
   * @param user the user to save
   */
  void save(User user);

  /**
   * Finds a user by username.
   *
   * @param username the username to search for
   * @return an Optional containing the user if found, empty otherwise
   */
  Optional<User> findByUsername(String username);

  /**
   * Checks if a user exists with the given username.
   *
   * @param username the username to check
   * @return true if the user exists, false otherwise
   */
  boolean existsByUsername(String username);
}

