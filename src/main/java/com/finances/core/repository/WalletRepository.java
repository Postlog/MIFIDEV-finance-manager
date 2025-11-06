package com.finances.core.repository;

import com.finances.core.domain.Wallet;
import java.util.Optional;

/** Repository interface for managing wallets. */
public interface WalletRepository {
  /**
   * Saves a wallet to the repository.
   *
   * @param wallet the wallet to save
   */
  void save(Wallet wallet);

  /**
   * Finds a wallet by user ID.
   *
   * @param userId the user ID to search for
   * @return an Optional containing the wallet if found, empty otherwise
   */
  Optional<Wallet> findByUserId(String userId);

  /**
   * Deletes a wallet by user ID.
   *
   * @param userId the user ID of the wallet to delete
   */
  void deleteByUserId(String userId);
}

