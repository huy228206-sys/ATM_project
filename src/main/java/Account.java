import java.util.ArrayList;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class Account {
    ArrayList<Transaction> transactionHistory;
    String accountName;
    String accountNumber;
    String pin;
    double balance;
    double dailyWithdrawn; // Daily cumulative transaction limit

    public String formatCurrency(double amount) {
        return String.format("%,.0f VNĐ", amount);
    }

    public Account(String name, String number, String pin, double balance) {
        this.accountName = name;
        this.accountNumber = number;
        this.pin = pin;
        this.balance = balance;
        this.transactionHistory = new ArrayList<>();
        this.dailyWithdrawn = 0;
    }

    // Deposit money
    public void deposit(double amount) { 
        if (amount < 5000) {
            System.out.println(ATM.getMessage("err_min_amount"));
            return;
        }
        this.balance += amount;
        Transaction t = new Transaction(ATM.getMessage("tx_deposit"), amount, this.balance);
        this.transactionHistory.add(t);
        t.saveToDatabase(this.accountNumber); // Save transaction to MongoDB
        System.out.println(ATM.getMessage("msg_deposit_success") + formatCurrency(amount));
    }

    // Withdraw money
    public void withdraw(double amount) {
        if (amount < 5000) {
            System.out.println(ATM.getMessage("err_min_amount"));
            return;
        }
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
            t.saveToDatabase(this.accountNumber); // Save transaction to MongoDB
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

    // Change PIN
    public void changePin(String newPin) {
        this.pin = newPin;
        System.out.println(ATM.getMessage("msg_pin_success"));
    }

    // Transfer money
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
            tOut.saveToDatabase(this.accountNumber); // Save sender's transaction to MongoDB
            
            targetAccount.balance += amount;
            Transaction tIn = new Transaction(ATM.getMessage("tx_received_from") + this.accountNumber, amount, targetAccount.balance);
            targetAccount.transactionHistory.add(tIn);
            tIn.saveToDatabase(targetAccount.accountNumber); // Save recipient's transaction to MongoDB

            System.out.println(ATM.getMessage("msg_transfer_success") + formatCurrency(amount));
        } else {
            System.out.println(ATM.getMessage("err_insufficient_balance"));
        }
    }

    // Load transaction history from MongoDB
    public void loadHistoryFromDatabase() {
        try {
            MongoDatabase database = DatabaseConnection.getDatabase();
            MongoCollection<Document> collection = database.getCollection("transactions");

            Document query = new Document("accountNumber", this.accountNumber);
            FindIterable<Document> results = collection.find(query);

            this.transactionHistory.clear();
            for (Document doc : results) {
                String type = doc.getString("type");
                double amount = doc.get("amount", Number.class).doubleValue();
                double balanceAfter = doc.get("balanceAfter", Number.class).doubleValue();
                Transaction t = new Transaction(type, amount, balanceAfter);
                this.transactionHistory.add(t);
            }

        } catch (Exception e) {
            System.out.println("-> Error loading transaction history from MongoDB: " + e.getMessage());
        }
    }
}