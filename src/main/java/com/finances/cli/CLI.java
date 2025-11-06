package com.finances.cli;

import com.finances.core.domain.Budget;
import com.finances.core.domain.Transaction;
import com.finances.core.domain.Wallet;
import com.finances.core.repository.WalletRepository;
import com.finances.core.service.AuthService;
import com.finances.core.service.BudgetService;
import com.finances.core.service.TransactionService;
import com.finances.infrastructure.notification.NotificationService;
import com.finances.infrastructure.persistence.FileStorage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/** Command-line interface for the Personal Finance Manager. */
public class CLI {
  private final Scanner scanner;
  private final AuthService authService;
  private final TransactionService transactionService;
  private final BudgetService budgetService;
  private final NotificationService notificationService;
  private final FileStorage fileStorage;
  private final WalletRepository walletRepository;
  private boolean running;

  public CLI(
      AuthService authService,
      TransactionService transactionService,
      BudgetService budgetService,
      NotificationService notificationService,
      FileStorage fileStorage,
      WalletRepository walletRepository) {
    this.scanner = new Scanner(System.in);
    this.authService = authService;
    this.transactionService = transactionService;
    this.budgetService = budgetService;
    this.notificationService = notificationService;
    this.fileStorage = fileStorage;
    this.walletRepository = walletRepository;
    this.running = true;
  }

  /** Starts the CLI application. */
  public void start() {
    printWelcome();

    while (running) {
      try {
        if (!authService.isAuthenticated()) {
          handleAuthMenu();
        } else {
          handleMainMenu();
        }
      } catch (Exception e) {
        System.out.println("Ошибка: " + e.getMessage());
      }
    }

    scanner.close();
  }

  private void printWelcome() {
    System.out.println("\n╔════════════════════════════════════════════════════════╗");
    System.out.println("║   Добро пожаловать в Personal Finance Manager!        ║");
    System.out.println("╚════════════════════════════════════════════════════════╝\n");
  }

  private void handleAuthMenu() {
    System.out.println("\n=== МЕНЮ АВТОРИЗАЦИИ ===");
    System.out.println("1. Войти");
    System.out.println("2. Зарегистрироваться");
    System.out.println("3. Выход");
    System.out.print("Выберите действие: ");

    String choice = scanner.nextLine().trim();

    switch (choice) {
      case "1":
        handleLogin();
        break;
      case "2":
        handleRegister();
        break;
      case "3":
        handleExit();
        break;
      default:
        System.out.println("Неверный выбор. Попробуйте снова.");
    }
  }

  private void handleLogin() {
    System.out.print("Логин: ");
    String username = scanner.nextLine().trim();

    System.out.print("Пароль: ");
    String password = scanner.nextLine().trim();

    if (authService.login(username, password)) {
      System.out.println("✓ Успешный вход!");

      // Load wallet from file
      try {
        Wallet wallet = fileStorage.loadWallet(username);
        walletRepository.save(wallet);
        System.out.println("✓ Данные кошелька загружены.");
      } catch (IOException e) {
        System.out.println("⚠ Не удалось загрузить данные кошелька: " + e.getMessage());
      }

      // Show notifications
      showNotifications();
    } else {
      System.out.println("✗ Неверный логин или пароль.");
    }
  }

  private void handleRegister() {
    System.out.print("Логин: ");
    String username = scanner.nextLine().trim();

    if (username.isEmpty()) {
      System.out.println("✗ Логин не может быть пустым.");
      return;
    }

    System.out.print("Пароль: ");
    String password = scanner.nextLine().trim();

    if (password.isEmpty()) {
      System.out.println("✗ Пароль не может быть пустым.");
      return;
    }

    if (authService.register(username, password)) {
      System.out.println("✓ Регистрация успешна! Теперь вы можете войти.");
    } else {
      System.out.println("✗ Пользователь с таким логином уже существует.");
    }
  }

  private void handleMainMenu() {
    String username = authService.getCurrentUser().getUsername();
    double balance = transactionService.getBalance(username);

    System.out.println("\n╔════════════════════════════════════════════════════════╗");
    System.out.println(String.format("║ Пользователь: %-40s ║", username));
    System.out.println(String.format("║ Баланс: %-43.2f ║", balance));
    System.out.println("╚════════════════════════════════════════════════════════╝");

    System.out.println("\n=== ГЛАВНОЕ МЕНЮ ===");
    System.out.println("1. Добавить доход");
    System.out.println("2. Добавить расход");
    System.out.println("3. Установить бюджет");
    System.out.println("4. Показать статистику");
    System.out.println("5. Показать бюджеты");
    System.out.println("6. Перевод другому пользователю");
    System.out.println("7. Экспорт данных");
    System.out.println("8. Показать уведомления");
    System.out.println("9. Справка (help)");
    System.out.println("0. Выход");
    System.out.print("Выберите действие: ");

    String choice = scanner.nextLine().trim();

    switch (choice) {
      case "1":
        handleAddIncome();
        break;
      case "2":
        handleAddExpense();
        break;
      case "3":
        handleSetBudget();
        break;
      case "4":
        handleShowStatistics();
        break;
      case "5":
        handleShowBudgets();
        break;
      case "6":
        handleTransfer();
        break;
      case "7":
        handleExport();
        break;
      case "8":
        showNotifications();
        break;
      case "9":
        handleHelp();
        break;
      case "0":
        handleLogout();
        break;
      default:
        System.out.println("Неверный выбор. Попробуйте снова.");
    }
  }

  private void handleAddIncome() {
    System.out.print("Категория дохода: ");
    String category = scanner.nextLine().trim();

    if (category.isEmpty()) {
      System.out.println("✗ Категория не может быть пустой.");
      return;
    }

    System.out.print("Сумма: ");
    String amountStr = scanner.nextLine().trim();

    double amount;
    try {
      amount = Double.parseDouble(amountStr);
      if (amount <= 0) {
        System.out.println("✗ Сумма должна быть положительной.");
        return;
      }
    } catch (NumberFormatException e) {
      System.out.println("✗ Неверный формат суммы.");
      return;
    }

    System.out.print("Описание (необязательно): ");
    String description = scanner.nextLine().trim();

    String username = authService.getCurrentUser().getUsername();
    transactionService.addIncome(username, category, amount, description);
    System.out.println(String.format("✓ Доход добавлен: %s - %.2f", category, amount));
  }

  private void handleAddExpense() {
    System.out.print("Категория расхода: ");
    String category = scanner.nextLine().trim();

    if (category.isEmpty()) {
      System.out.println("✗ Категория не может быть пустой.");
      return;
    }

    System.out.print("Сумма: ");
    String amountStr = scanner.nextLine().trim();

    double amount;
    try {
      amount = Double.parseDouble(amountStr);
      if (amount <= 0) {
        System.out.println("✗ Сумма должна быть положительной.");
        return;
      }
    } catch (NumberFormatException e) {
      System.out.println("✗ Неверный формат суммы.");
      return;
    }

    System.out.print("Описание (необязательно): ");
    String description = scanner.nextLine().trim();

    String username = authService.getCurrentUser().getUsername();
    transactionService.addExpense(username, category, amount, description);
    System.out.println(String.format("✓ Расход добавлен: %s - %.2f", category, amount));

    // Check and notify about budget
    notificationService.checkAndNotifyAfterTransaction(username, category);
  }

  private void handleSetBudget() {
    System.out.print("Категория: ");
    String category = scanner.nextLine().trim();

    if (category.isEmpty()) {
      System.out.println("✗ Категория не может быть пустой.");
      return;
    }

    System.out.print("Лимит бюджета: ");
    String limitStr = scanner.nextLine().trim();

    double limit;
    try {
      limit = Double.parseDouble(limitStr);
      if (limit < 0) {
        System.out.println("✗ Лимит не может быть отрицательным.");
        return;
      }
    } catch (NumberFormatException e) {
      System.out.println("✗ Неверный формат суммы.");
      return;
    }

    String username = authService.getCurrentUser().getUsername();
    budgetService.setBudget(username, category, limit);
    System.out.println(String.format("✓ Бюджет установлен: %s - %.2f", category, limit));
  }

  private void handleShowStatistics() {
    String username = authService.getCurrentUser().getUsername();

    System.out.println("\n╔════════════════════════════════════════════════════════╗");
    System.out.println("║                   ФИНАНСОВАЯ СТАТИСТИКА                ║");
    System.out.println("╚════════════════════════════════════════════════════════╝");

    double totalIncome = transactionService.getTotalIncome(username);
    double totalExpense = transactionService.getTotalExpense(username);
    double balance = transactionService.getBalance(username);

    System.out.println(String.format("\nОбщий доход: %.2f", totalIncome));
    System.out.println(String.format("Общие расходы: %.2f", totalExpense));
    System.out.println(String.format("Баланс: %.2f", balance));

    System.out.println("\n--- Доходы по категориям ---");
    Map<String, Double> incomeByCategory = transactionService.getIncomeByCategory(username);
    if (incomeByCategory.isEmpty()) {
      System.out.println("  (нет данных)");
    } else {
      incomeByCategory.forEach(
          (category, amount) -> System.out.println(String.format("  %s: %.2f", category, amount)));
    }

    System.out.println("\n--- Расходы по категориям ---");
    Map<String, Double> expenseByCategory = transactionService.getExpenseByCategory(username);
    if (expenseByCategory.isEmpty()) {
      System.out.println("  (нет данных)");
    } else {
      expenseByCategory.forEach(
          (category, amount) -> System.out.println(String.format("  %s: %.2f", category, amount)));
    }

    // Option to filter by categories
    System.out.print("\nФильтровать по категориям? (да/нет): ");
    String filterChoice = scanner.nextLine().trim().toLowerCase();

    if (filterChoice.equals("да") || filterChoice.equals("yes") || filterChoice.equals("y")) {
      System.out.print("Введите категории через запятую: ");
      String categoriesStr = scanner.nextLine().trim();

      if (!categoriesStr.isEmpty()) {
        List<String> categories =
            Arrays.stream(categoriesStr.split(",")).map(String::trim).toList();

        double filteredIncome = transactionService.getIncomeForCategories(username, categories);
        double filteredExpense = transactionService.getExpenseForCategories(username, categories);

        System.out.println("\n--- Фильтрованная статистика ---");
        System.out.println(String.format("Доход: %.2f", filteredIncome));
        System.out.println(String.format("Расходы: %.2f", filteredExpense));

        // Check for unknown categories
        List<String> knownCategories =
            transactionService.getTransactions(username).stream()
                .map(Transaction::getCategory)
                .distinct()
                .toList();
        List<String> unknownCategories =
            categories.stream().filter(c -> !knownCategories.contains(c)).toList();

        if (!unknownCategories.isEmpty()) {
          System.out.println(
              "\n⚠ Категории не найдены: " + String.join(", ", unknownCategories));
        }
      }
    }
  }

  private void handleShowBudgets() {
    String username = authService.getCurrentUser().getUsername();
    Map<String, Budget> budgets = budgetService.getAllBudgets(username);

    System.out.println("\n╔════════════════════════════════════════════════════════╗");
    System.out.println("║                   БЮДЖЕТЫ ПО КАТЕГОРИЯМ                ║");
    System.out.println("╚════════════════════════════════════════════════════════╝");

    if (budgets.isEmpty()) {
      System.out.println("\nБюджеты не установлены.");
      return;
    }

    System.out.println();
    for (Map.Entry<String, Budget> entry : budgets.entrySet()) {
      String category = entry.getKey();
      Budget budget = entry.getValue();
      double remaining = budgetService.getRemainingBudget(username, category);
      double spent = budget.getLimit() - remaining;
      double percentage = budgetService.getBudgetUsagePercentage(username, category);

      String status = remaining >= 0 ? "✓" : "✗";
      System.out.println(
          String.format(
              "%s %s: Лимит=%.2f, Потрачено=%.2f, Осталось=%.2f (%.0f%%)",
              status, category, budget.getLimit(), spent, remaining, percentage));
    }
  }

  private void handleTransfer() {
    System.out.print("Логин получателя: ");
    String toUser = scanner.nextLine().trim();

    if (toUser.isEmpty()) {
      System.out.println("✗ Логин получателя не может быть пустым.");
      return;
    }

    String username = authService.getCurrentUser().getUsername();
    if (toUser.equals(username)) {
      System.out.println("✗ Нельзя перевести самому себе.");
      return;
    }

    // Check if receiver exists
    if (!fileStorage.walletExists(toUser)) {
      System.out.println("✗ Пользователь не найден.");
      return;
    }

    System.out.print("Сумма перевода: ");
    String amountStr = scanner.nextLine().trim();

    double amount;
    try {
      amount = Double.parseDouble(amountStr);
      if (amount <= 0) {
        System.out.println("✗ Сумма должна быть положительной.");
        return;
      }
    } catch (NumberFormatException e) {
      System.out.println("✗ Неверный формат суммы.");
      return;
    }

    double balance = transactionService.getBalance(username);
    if (balance < amount) {
      System.out.println(
          String.format("✗ Недостаточно средств. Ваш баланс: %.2f, требуется: %.2f", balance, amount));
      return;
    }

    System.out.print("Описание (необязательно): ");
    String description = scanner.nextLine().trim();

    try {
      // Load receiver's wallet
      Wallet receiverWallet = fileStorage.loadWallet(toUser);
      walletRepository.save(receiverWallet);

      transactionService.transfer(username, toUser, amount, description);

      // Save both wallets
      Wallet senderWallet = walletRepository.findByUserId(username).orElseThrow();
      receiverWallet = walletRepository.findByUserId(toUser).orElseThrow();

      fileStorage.saveWallet(senderWallet);
      fileStorage.saveWallet(receiverWallet);

      System.out.println(String.format("✓ Перевод выполнен: %.2f -> %s", amount, toUser));
    } catch (Exception e) {
      System.out.println("✗ Ошибка при переводе: " + e.getMessage());
    }
  }

  private void handleExport() {
    System.out.println("\n=== ЭКСПОРТ ДАННЫХ ===");
    System.out.println("1. Экспорт в CSV");
    System.out.println("2. Экспорт в JSON");
    System.out.print("Выберите формат: ");

    String choice = scanner.nextLine().trim();
    String username = authService.getCurrentUser().getUsername();

    Wallet wallet = walletRepository.findByUserId(username).orElseThrow();

    try {
      if (choice.equals("1")) {
        String filename = username + "_export.csv";
        fileStorage.exportToCSV(wallet, filename);
        System.out.println("✓ Данные экспортированы в " + filename);
      } else if (choice.equals("2")) {
        String filename = username + "_export.json";
        fileStorage.exportToJSON(wallet, filename);
        System.out.println("✓ Данные экспортированы в " + filename);
      } else {
        System.out.println("✗ Неверный выбор.");
      }
    } catch (IOException e) {
      System.out.println("✗ Ошибка при экспорте: " + e.getMessage());
    }
  }

  private void showNotifications() {
    String username = authService.getCurrentUser().getUsername();
    List<String> notifications = notificationService.getNotifications(username);

    if (!notifications.isEmpty()) {
      System.out.println("\n╔════════════════════════════════════════════════════════╗");
      System.out.println("║                      УВЕДОМЛЕНИЯ                       ║");
      System.out.println("╚════════════════════════════════════════════════════════╝");
      notifications.forEach(System.out::println);
    }
  }

  private void handleHelp() {
    System.out.println("\n╔════════════════════════════════════════════════════════╗");
    System.out.println("║                     СПРАВКА (HELP)                     ║");
    System.out.println("╚════════════════════════════════════════════════════════╝");
    System.out.println("\nДоступные команды:");
    System.out.println(
        "  1. Добавить доход - добавить новую транзакцию дохода с категорией и суммой");
    System.out.println(
        "  2. Добавить расход - добавить новую транзакцию расхода с категорией и суммой");
    System.out.println("  3. Установить бюджет - установить лимит расходов для категории");
    System.out.println("  4. Показать статистику - отобразить доходы и расходы по категориям");
    System.out.println("  5. Показать бюджеты - отобразить установленные бюджеты и их статус");
    System.out.println("  6. Перевод - перевести деньги другому пользователю");
    System.out.println("  7. Экспорт данных - экспортировать данные в CSV или JSON формат");
    System.out.println("  8. Показать уведомления - показать предупреждения о бюджетах");
    System.out.println("  0. Выход - сохранить данные и выйти из приложения");
    System.out.println("\nПримеры:");
    System.out.println("  • Категории доходов: Зарплата, Фриланс, Бонус");
    System.out.println("  • Категории расходов: Еда, Транспорт, Развлечения, Коммунальные услуги");
    System.out.println("  • При превышении 80% бюджета вы получите предупреждение");
    System.out.println("  • При превышении 100% бюджета вы получите критическое уведомление");
  }

  private void handleLogout() {
    String username = authService.getCurrentUser().getUsername();

    try {
      Wallet wallet = walletRepository.findByUserId(username).orElseThrow();
      fileStorage.saveWallet(wallet);
      System.out.println("✓ Данные сохранены.");
    } catch (Exception e) {
      System.out.println("⚠ Ошибка при сохранении данных: " + e.getMessage());
    }

    authService.logout();
    System.out.println("✓ Вы вышли из системы.");
  }

  private void handleExit() {
    System.out.println("До свидания!");
    running = false;
  }
}

