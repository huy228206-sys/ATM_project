import java.util.ArrayList;

public class Account {
    ArrayList<Transaction> transactionHistory;
    String accountName;
    String accountNumber;
    String pin;
    double balance;
    double dailyWithdrawn; // 5. Track daily total transaction limit

    // Helper method to dynamically format currency to VNĐ instead of old US Locale
    public String formatCurrency(double amount) {
        return String.format("%,.0f VNĐ", amount);
    }

    public Account(String name, String number, String pin, double balance){
        accountName = name;
        accountNumber = number;
        this.pin = pin;
        this.balance = balance;
        this.transactionHistory = new ArrayList<>();
        this.dailyWithdrawn = 0;
    }

    // Deposit money
    public void deposit(double amount) { 
        if (amount < 5000){
            System.out.println(ATM.getMessage("err_min_amount"));
            return;
        }
        this.balance += amount;
        Transaction t = new Transaction(ATM.getMessage("tx_deposit"), amount, this.balance);
        this.transactionHistory.add(t);
        System.out.println(ATM.getMessage("msg_deposit_success") + formatCurrency(amount));
    }

    // Withdraw money
    public void withdraw(double amount) {
        // Enforce individual transaction minimum threshold
        if (amount < 5000) {
            System.out.println(ATM.getMessage("err_min_amount"));
            return;
        }
        // Enforce maximum 50,000,000 VNĐ cumulative daily limit
        if (this.dailyWithdrawn + amount > 50000000) {
            System.out.println(ATM.getMessage("err_daily_limit"));
            System.out.println(ATM.getMessage("msg_daily_withdrawn") + formatCurrency(this.dailyWithdrawn));
            return; 
        }
        if (this.balance >= amount) {
            this.balance -= amount;
            this.dailyWithdrawn += amount;
            Transaction t = new Transaction(ATM.getMessage("tx_withdraw"), amount, this.balance);
            this.transactionHistory.add(t);
            System.out.println(ATM.getMessage("msg_withdraw_success") + formatCurrency(amount));
        } else {
            System.out.println(ATM.getMessage("err_insufficient_balance"));
        }
    }

    // Display transaction history
    public void displayHistory() {
        System.out.println(ATM.getMessage("menu_history_title"));
        if (this.transactionHistory.isEmpty()) {
            System.out.println(ATM.getMessage("msg_no_history"));
        } else {
            for (Transaction t : this.transactionHistory) {
                System.out.println("- " + ATM.getMessage("lbl_type") + ": " + t.type + 
                                   " | " + ATM.getMessage("lbl_amount") + ": " + formatCurrency(t.amount) + 
                                   " | " + ATM.getMessage("lbl_balance_after") + ": " + formatCurrency(t.balanceAfter));
            }
        }
        System.out.println("=========================");
    }

    // Feature 2: Change PIN logic
    public void changePin (String newPin){
        this.pin = newPin;
        System.out.println(ATM.getMessage("msg_pin_success"));
    }

    // Feature 3: Transfer money to another account
    public void transfer(Account targetAccount, double amount) {
        if (amount < 5000) {
            System.out.println(ATM.getMessage("err_min_amount"));
            return;
        }
        if (this.dailyWithdrawn + amount > 50000000) {
            System.out.println(ATM.getMessage("err_daily_limit"));
            return;
        }
        if (this.balance >= amount) {
            this.balance -= amount;
            this.dailyWithdrawn += amount;
            Transaction tOut = new Transaction(ATM.getMessage("tx_transfer_to") + targetAccount.accountNumber, amount, this.balance);
            this.transactionHistory.add(tOut);
            targetAccount.balance += amount;
            Transaction tIn = new Transaction(ATM.getMessage("tx_received_from") + this.accountNumber, amount, targetAccount.balance);
            targetAccount.transactionHistory.add(tIn);
            
            System.out.println(ATM.getMessage("msg_transfer_success") + formatCurrency(amount));
        } else {
            System.out.println(ATM.getMessage("err_insufficient_balance"));
        }
    }
}