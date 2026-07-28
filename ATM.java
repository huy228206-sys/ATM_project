import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Properties;

public class ATM {
    ArrayList<Account> accounts; 
    public static Properties messages = new Properties(); // Multi-language system data map

    public ATM() {
        this.accounts = new ArrayList<>();
    }
    
    // Load local message files dynamically based on session preference
    public static void loadLanguage(String lang) {
        try (BufferedReader reader = new BufferedReader(new FileReader("language_" + lang + ".message"))) {
            messages.load(reader);
        } catch (IOException e) {
            System.out.println("Error loading translation file: " + e.getMessage());
        }
    }

    // Helper method to fetch mapped translations
    public static String getMessage(String key) {
        return messages.getProperty(key, key);
    }

    // Add account to memory array
    public void addAccount(Account acc) {
        this.accounts.add(acc);
    }

    // Execute ATM life-cycle
    public void start() {
        Scanner scanner = new Scanner(System.in);
        int attempts = 0; // Login tracking error index
        
        while (true) { 
            // Multi-language selection triggered at every single new session start
            System.out.println("\nSelect Language / Chon ngon ngu:");
            System.out.println("1. English");
            System.out.println("2. Vietnamese");
            System.out.print("Choose / Chon (1-2): ");
            String langChoice = scanner.nextLine().trim();
            if ("1".equals(langChoice)) {
                loadLanguage("en");
            } else {
                loadLanguage("vn");
            }

            System.out.println("\n" + getMessage("welcome_title"));
            System.out.print(getMessage("prompt_account_num"));
            String inputAccNum = scanner.nextLine();
            
            // Safe termination path
            if (inputAccNum.equalsIgnoreCase("q")) {
                System.out.println(getMessage("msg_system_off"));
                break;
            }
            
            System.out.print(getMessage("prompt_pin"));
            String inputPin = scanner.nextLine();

            Account currentAccount = null;
            // Scan credentials match
            for (Account acc : this.accounts) {
                if (acc.accountNumber.equals(inputAccNum) && acc.pin.equals(inputPin)) {
                    currentAccount = acc; 
                    break; 
                }
            }

            if (currentAccount != null) {
                System.out.println("\n" + getMessage("msg_login_success") + currentAccount.accountName + "!");
                
                // Reset metrics
                attempts = 0; 
                
                showMenu(currentAccount, scanner);
            } else {
                attempts++; 
                System.out.println(getMessage("err_login_failed") + (3 - attempts) + " " + getMessage("lbl_attempts_left") + "\n");
                
                // Security locking bounds
                if (attempts >= 3) {
                    System.out.println(getMessage("err_system_locked"));
                    break; 
                }
            }
        }
        scanner.close(); 
    }

    // Render interactive action panel
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

    // Input loop validation utility for precision numbers
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

    // Input validation utility for clean selections
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

    // Read flat configuration maps from data path
    public void loadAccounts() {
        File file = new File("accounts.txt");
        if (!file.exists()) {
            System.out.println("Data file not found. Initializing default data...");
            // Standard VNĐ mock amounts initialized for fallback
            addAccount(new Account("Huy", "1001", "4321", 5000000.0));
            addAccount(new Account("Hoa", "1002", "1111", 10000000.0));
            addAccount(new Account("Nam", "1003", "9999", 500000.0));
            saveAccounts(); 
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(","); 
                if (parts.length == 4) {
                    String number = parts[0];
                    String name = parts[1];
                    String pin = parts[2];
                    double balance = Double.parseDouble(parts[3]);
                    
                    addAccount(new Account(name, number, pin, balance));
                }
            }
            System.out.println("Data loaded from file successfully!");
        } catch (Exception e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    // Persistence layer writer out to flat file target
    public void saveAccounts() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("accounts.txt"))) {
            for (Account acc : accounts) {
                writer.println(acc.accountNumber + "," + acc.accountName + "," + acc.pin + "," + acc.balance);
            }
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }
}