package com.finances.infrastructure.persistence;

import com.finances.core.domain.Wallet;
import com.finances.core.repository.WalletRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** In-memory implementation of the WalletRepository. */
public class InMemoryWalletRepository implements WalletRepository {
  private final Map<String, Wallet> wallets = new HashMap<>();

  @Override
  public void save(Wallet wallet) {
    wallets.put(wallet.getUserId(), wallet);
  }

  @Override
  public Optional<Wallet> findByUserId(String userId) {
    return Optional.ofNullable(wallets.get(userId));
  }

  @Override
  public void deleteByUserId(String userId) {
    wallets.remove(userId);
  }
}

