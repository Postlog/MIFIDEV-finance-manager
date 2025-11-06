package com.finances;

import com.finances.cli.CLI;
import com.finances.core.repository.UserRepository;
import com.finances.core.repository.WalletRepository;
import com.finances.core.service.AuthService;
import com.finances.core.service.BudgetService;
import com.finances.core.service.TransactionService;
import com.finances.infrastructure.notification.NotificationService;
import com.finances.infrastructure.persistence.FileStorage;
import com.finances.infrastructure.persistence.InMemoryUserRepository;
import com.finances.infrastructure.persistence.InMemoryWalletRepository;

/** Main entry point for the Personal Finance Manager application. */
public class Main {
  public static void main(String[] args) {
    // Initialize repositories
    UserRepository userRepository = new InMemoryUserRepository();
    WalletRepository walletRepository = new InMemoryWalletRepository();

    // Initialize services
    AuthService authService = new AuthService(userRepository, walletRepository);
    TransactionService transactionService = new TransactionService(walletRepository);
    BudgetService budgetService = new BudgetService(walletRepository);
    NotificationService notificationService =
        new NotificationService(budgetService, transactionService);

    // Initialize file storage
    FileStorage fileStorage = new FileStorage();

    // Initialize and start CLI
    CLI cli =
        new CLI(
            authService,
            transactionService,
            budgetService,
            notificationService,
            fileStorage,
            walletRepository);

    cli.start();
  }
}

