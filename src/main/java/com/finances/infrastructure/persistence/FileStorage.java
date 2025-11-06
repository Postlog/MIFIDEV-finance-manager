package com.finances.infrastructure.persistence;

import com.finances.core.domain.Budget;
import com.finances.core.domain.Transaction;
import com.finances.core.domain.TransactionType;
import com.finances.core.domain.Wallet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/** Service for persisting wallet data to JSON files. */
public class FileStorage {
  private static final String STORAGE_DIR = "wallets";
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
  private final Gson gson;

  public FileStorage() {
    this.gson = new GsonBuilder().setPrettyPrinting().create();
    createStorageDirectory();
  }

  /**
   * Saves a wallet to a file.
   *
   * @param wallet the wallet to save
   * @throws IOException if an I/O error occurs
   */
  public void saveWallet(Wallet wallet) throws IOException {
    String filename = getWalletFilename(wallet.getUserId());
    JsonObject json = new JsonObject();

    json.addProperty("userId", wallet.getUserId());
    json.addProperty("balance", wallet.getBalance());
    json.addProperty("totalIncome", wallet.getTotalIncome());
    json.addProperty("totalExpense", wallet.getTotalExpense());

    // Serialize transactions
    JsonArray transactionsArray = new JsonArray();
    for (Transaction transaction : wallet.getTransactions()) {
      JsonObject transactionJson = new JsonObject();
      transactionJson.addProperty("id", transaction.getId());
      transactionJson.addProperty("type", transaction.getType().name());
      transactionJson.addProperty("category", transaction.getCategory());
      transactionJson.addProperty("amount", transaction.getAmount());
      transactionJson.addProperty("timestamp", transaction.getTimestamp().format(DATE_FORMATTER));
      transactionJson.addProperty("description", transaction.getDescription());
      transactionsArray.add(transactionJson);
    }
    json.add("transactions", transactionsArray);

    // Serialize budgets
    JsonObject budgetsJson = new JsonObject();
    for (Map.Entry<String, Budget> entry : wallet.getAllBudgets().entrySet()) {
      budgetsJson.addProperty(entry.getKey(), entry.getValue().getLimit());
    }
    json.add("budgets", budgetsJson);

    try (FileWriter writer = new FileWriter(filename)) {
      gson.toJson(json, writer);
    }
  }

  /**
   * Loads a wallet from a file.
   *
   * @param userId the user ID
   * @return the loaded wallet or a new wallet if file doesn't exist
   * @throws IOException if an I/O error occurs
   */
  public Wallet loadWallet(String userId) throws IOException {
    String filename = getWalletFilename(userId);
    File file = new File(filename);

    if (!file.exists()) {
      return new Wallet(userId);
    }

    try (FileReader reader = new FileReader(filename)) {
      JsonObject json = gson.fromJson(reader, JsonObject.class);
      Wallet wallet = new Wallet(userId);

      // Deserialize transactions
      if (json.has("transactions")) {
        JsonArray transactionsArray = json.getAsJsonArray("transactions");
        for (int i = 0; i < transactionsArray.size(); i++) {
          JsonObject transactionJson = transactionsArray.get(i).getAsJsonObject();
          Transaction transaction =
              new Transaction(
                  transactionJson.get("id").getAsString(),
                  TransactionType.valueOf(transactionJson.get("type").getAsString()),
                  transactionJson.get("category").getAsString(),
                  transactionJson.get("amount").getAsDouble(),
                  LocalDateTime.parse(
                      transactionJson.get("timestamp").getAsString(), DATE_FORMATTER),
                  transactionJson.has("description")
                      ? transactionJson.get("description").getAsString()
                      : "");
          wallet.addTransaction(transaction);
        }
      }

      // Deserialize budgets
      if (json.has("budgets")) {
        JsonObject budgetsJson = json.getAsJsonObject("budgets");
        for (String category : budgetsJson.keySet()) {
          double limit = budgetsJson.get(category).getAsDouble();
          wallet.setBudget(category, limit);
        }
      }

      return wallet;
    }
  }

  /**
   * Deletes a wallet file.
   *
   * @param userId the user ID
   * @return true if the file was deleted, false otherwise
   */
  public boolean deleteWallet(String userId) {
    String filename = getWalletFilename(userId);
    File file = new File(filename);
    return file.delete();
  }

  /**
   * Checks if a wallet file exists.
   *
   * @param userId the user ID
   * @return true if the wallet file exists, false otherwise
   */
  public boolean walletExists(String userId) {
    String filename = getWalletFilename(userId);
    File file = new File(filename);
    return file.exists();
  }

  /**
   * Exports wallet data to a CSV file.
   *
   * @param wallet the wallet to export
   * @param outputPath the output file path
   * @throws IOException if an I/O error occurs
   */
  public void exportToCSV(Wallet wallet, String outputPath) throws IOException {
    try (FileWriter writer = new FileWriter(outputPath)) {
      writer.write("Type,Category,Amount,Date,Description\n");
      for (Transaction transaction : wallet.getTransactions()) {
        writer.write(
            String.format(
                "%s,%s,%.2f,%s,%s\n",
                transaction.getType(),
                transaction.getCategory(),
                transaction.getAmount(),
                transaction.getTimestamp().toLocalDate(),
                transaction.getDescription().replace(",", ";")));
      }
    }
  }

  /**
   * Exports wallet data to a JSON file.
   *
   * @param wallet the wallet to export
   * @param outputPath the output file path
   * @throws IOException if an I/O error occurs
   */
  public void exportToJSON(Wallet wallet, String outputPath) throws IOException {
    JsonObject json = new JsonObject();

    json.addProperty("userId", wallet.getUserId());
    json.addProperty("balance", wallet.getBalance());
    json.addProperty("totalIncome", wallet.getTotalIncome());
    json.addProperty("totalExpense", wallet.getTotalExpense());

    JsonArray transactionsArray = new JsonArray();
    for (Transaction transaction : wallet.getTransactions()) {
      JsonObject transactionJson = new JsonObject();
      transactionJson.addProperty("id", transaction.getId());
      transactionJson.addProperty("type", transaction.getType().name());
      transactionJson.addProperty("category", transaction.getCategory());
      transactionJson.addProperty("amount", transaction.getAmount());
      transactionJson.addProperty("timestamp", transaction.getTimestamp().format(DATE_FORMATTER));
      transactionJson.addProperty("description", transaction.getDescription());
      transactionsArray.add(transactionJson);
    }
    json.add("transactions", transactionsArray);

    JsonObject budgetsJson = new JsonObject();
    for (Map.Entry<String, Budget> entry : wallet.getAllBudgets().entrySet()) {
      budgetsJson.addProperty(entry.getKey(), entry.getValue().getLimit());
    }
    json.add("budgets", budgetsJson);

    try (FileWriter writer = new FileWriter(outputPath)) {
      gson.toJson(json, writer);
    }
  }

  private void createStorageDirectory() {
    File dir = new File(STORAGE_DIR);
    if (!dir.exists()) {
      dir.mkdirs();
    }
  }

  private String getWalletFilename(String userId) {
    return STORAGE_DIR + File.separator + userId + ".wallet";
  }
}

