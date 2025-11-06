package com.finances.infrastructure.persistence;

import com.finances.core.domain.User;
import com.finances.core.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** In-memory implementation of the UserRepository. */
public class InMemoryUserRepository implements UserRepository {
  private final Map<String, User> users = new HashMap<>();

  @Override
  public void save(User user) {
    users.put(user.getUsername(), user);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return Optional.ofNullable(users.get(username));
  }

  @Override
  public boolean existsByUsername(String username) {
    return users.containsKey(username);
  }
}

