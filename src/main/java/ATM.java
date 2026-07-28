import java.util.ArrayList;
import java.util.Scanner;
import java.util.Properties;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

public class ATM {
    ArrayList<Account> accounts; 
    public static Properties messages = new Properties();

    public ATM() {
        this.accounts = new ArrayList<>();
    }

    public static void loadLanguage(String lang) {
        LanguageManager.loadLanguage(lang);
    }

    public static String getMessage(String key) {
        return LanguageManager.getMessage(key);
    }

    public void addAccount(Account acc) {
        this.accounts.add(acc);
    }

    public void start() {
    Scanner scanner = new Scanner(System.in);
    while (true) {
        System.out.println("\nSelect Language / Chon ngon ngu:");
        System.out.println("1. English");
        System.out.println("2. Vietnamese");
        System.out.print("Choose / Chon (1-2): ");
        String langChoice = scanner.nextLine().trim();
        if ("1".equals(langChoice)) {
            LanguageManager.loadLanguage("en");
        } else {
            LanguageManager.loadLanguage("vn");
        }

        int attempts = 0; 
        while (attempts < 3) {
            System.out.println("\n" + getMessage("welcome_title"));
            System.out.print(getMessage("prompt_account_num"));
            String inputAccNum = scanner.nextLine();
            if (inputAccNum.equalsIgnoreCase("q")) {
                System.out.println(getMessage("msg_system_off"));
                scanner.close();
                return; 
            }

            System.out.print(getMessage("prompt_pin"));
            String inputPin = scanner.nextLine();

            Account currentAccount = null;
            for (Account acc : this.accounts) {
                if (acc.accountNumber.equals(inputAccNum) && acc.pin.equals(inputPin)) {
                    currentAccount = acc;
                    break;
                }
            }

            if (currentAccount != null) {
                System.out.println("\n" + getMessage("msg_login_success") + currentAccount.accountName + "!");
                currentAccount.loadHistoryFromDatabase();
                showMenu(currentAccount, scanner);
                break; 
            } else {
                attempts++;
                if (attempts < 3) {
                    System.out.println(getMessage("err_login_failed") + (3 - attempts) + " " + getMessage("lbl_attempts_left") + "\n");

                } else {
                    System.out.println(getMessage("err_system_locked"));
                   
                }
            }
        }
    }
}

    public void showMenu(Account acc, Scanner scanner) {
        int choice = 0;
        while (choice != 7) {
            System.out.println("\n" + getMessage("menu_title"));
            System.out.println("1. " + getMessage("menu_check_balance"));
            System.out.println("2. " + getMessage("menu_deposit"));
            System.out.println("3. " + getMessage("menu_withdraw"));
            System.out.println("4. " + getMessage("menu_transfer"));
            System.out.println("5. " + getMessage("menu_history"));
            System.out.println("6. " + getMessage("menu_change_pin"));
            System.out.println("7. " + getMessage("menu_logout"));
            choice = getIntInput(scanner, getMessage("prompt_choose_option"));
            
            if (choice == 1) {
                System.out.println(getMessage("msg_current_balance") + acc.formatCurrency(acc.balance));

            } else if (choice == 2) {
                double amount = getDoubleInput(scanner, getMessage("prompt_deposit_amount"));
                acc.deposit(amount);
                saveAccounts();
            } else if (choice == 3) {
                double amount = getDoubleInput(scanner, getMessage("prompt_withdraw_amount"));
                acc.withdraw(amount);
                saveAccounts();
            } else if (choice == 5) {
                acc.displayHistory();
            } else if (choice == 6) {
                System.out.println(getMessage("prompt_new_pin"));
                String newPin = scanner.nextLine();
                acc.changePin(newPin);
                saveAccounts();
            } else if (choice == 7) {
                System.out.println(getMessage("msg_logout_success"));

            } else if (choice == 4) { 
                System.out.print(getMessage("prompt_target_account"));
                String targetAccNum = scanner.nextLine();
                
                Account targetAccount = findAccount(targetAccNum);
                if (targetAccount == null) {
                    System.out.println(getMessage("err_target_not_found"));
                } else if (targetAccount.accountNumber.equals(acc.accountNumber)) {
                    System.out.println(getMessage("err_transfer_self"));
                } else {
                    double amount = getDoubleInput(scanner, getMessage("prompt_transfer_amount"));
                    acc.transfer(targetAccount, amount);
                    saveAccounts();
                } 
            } else {
                System.out.println(getMessage("err_invalid_choice"));
            }
        }
    }

    private double getDoubleInput(Scanner scanner, String prompt) {
        double value = 0;
        boolean valid = false;
        while (!valid) {
            System.out.print(prompt);
            try {
                value = Double.parseDouble(scanner.nextLine());
                valid = true;
            } catch (NumberFormatException e) {
                System.out.println(getMessage("err_numeric_only"));
            }
        }
        return value;
    }

    private int getIntInput(Scanner scanner, String prompt) {
        int value = 0;
        boolean valid = false;
        while (!valid) {
            System.out.print(prompt);
            try {
                value = Integer.parseInt(scanner.nextLine());
                valid = true;
            } catch (NumberFormatException e) {
                System.out.println(getMessage("err_integer_only"));
            }
        }
        return value;
    }

    public Account findAccount(String accountNumber) {
        for (Account acc : this.accounts) {
            if (acc.accountNumber.equals(accountNumber)) {
                return acc;
            }
        }
        return null;
    }

    // // Load account from MongoDB
    public void loadAccounts() {
        try {
            MongoDatabase database = DatabaseConnection.getDatabase();
            MongoCollection<Document> collection = database.getCollection("accounts");

            this.accounts.clear();
            FindIterable<Document> results = collection.find();

            boolean hasData = false;
            for (Document doc : results) {
                hasData = true;
                String number = doc.getString("accountNumber");
                String name = doc.getString("accountName");
                String pin = doc.getString("pin");
                double balance = doc.get("balance", Number.class).doubleValue();
                
                addAccount(new Account(name, number, pin, balance));
            }

        
            if (!hasData) {
                System.out.println("-> MongoDB chua co du lieu tai khoan. Dang khoi tao du lieu mac dinh...");
                Account acc1 = new Account("Huy", "1001", "4321", 5000000.0);
                Account acc2 = new Account("Hoa", "1002", "1111", 10000000.0);
                Account acc3 = new Account("Nam", "1003", "9999", 500000.0);

                addAccount(acc1);
                addAccount(acc2);
                addAccount(acc3);

                saveAccounts();
            } else {
                System.out.println("-> Tai du lieu tai khoan tu MongoDB thanh cong!");
            }

        } catch (Exception e) {
            System.out.println("-> Loi khi tai du lieu tai khoan tu MongoDB: " + e.getMessage());
        }
    }

    public void saveAccountToDatabase(Account acc) {
        try {
            MongoDatabase database = DatabaseConnection.getDatabase();
            MongoCollection<Document> collection = database.getCollection("accounts");

            Document filter = new Document("accountNumber", acc.accountNumber);
            Document update = new Document("$set", new Document()
                    .append("accountNumber", acc.accountNumber)
                    .append("accountName", acc.accountName)
                    .append("pin", acc.pin)
                    .append("balance", acc.balance));

           
            collection.updateOne(filter, update, new UpdateOptions().upsert(true));
        } catch (Exception e) {
            System.out.println("-> Loi khi cap nhat tai khoan vao MongoDB: " + e.getMessage());
        }
    }

    
    public void saveAccounts() {
        for (Account acc : this.accounts) {
            saveAccountToDatabase(acc);
        }
    }
}