import java.util.ArrayList;
import java.text.NumberFormat;
import java.util.Locale;
public class Account {
    ArrayList<Transaction> transactionHistory;
    String accountName;
    String accountNumber;
    String pin;
    double balance;
    double dailyWithdrawn; // 5.Gioi han tien rut
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US); //6.dinh dang tien te
    public Account(String name, String number, String pin, double balance){
        accountName = name;
        accountNumber = number;
        this.pin = pin;
        this.balance = balance;
        this.transactionHistory = new ArrayList<>();
        this.dailyWithdrawn = 0;
    }
    //Nap tien
    public void deposit(double amount) { 
        if (amount<=0){
            System.out.println("Loi:So tien phai nap lon hon 0.");
            return;
        }
        this.balance += amount;
        Transaction t = new Transaction("Nap tien", amount, this.balance);
        this.transactionHistory.add(t);
        System.out.println("Nap tien thanh cong: +" + currencyFormat.format(amount));
    }
    //Rut tien
    public void withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Loi: So tien rut phai lon hon 0.");
            return;
        }
        //Tinh nag 5:gioi han tien rut
        if (this.dailyWithdrawn + amount > 500) {
            System.out.println("Loi: Vuot qua han muc rut tien! Ban chi duoc rut toi da $500.00 moi phien.");
            System.out.println("So tien ban da rut hom nay la: " + currencyFormat.format(this.dailyWithdrawn));
            return; 
        }
        if (this.balance >= amount) {
            this.balance -= amount;
            this.dailyWithdrawn += amount;
            Transaction t = new Transaction("Rut tien", amount, this.balance);
            this.transactionHistory.add(t);
            System.out.println("Rut tien thanh cong: -" + currencyFormat.format(amount));
        } else {
         
            System.out.println("Giao dich that bai! So du tai khoan khong du.");
        }
    }
    //lich su giao dich
    public void displayHistory() {
        System.out.println("=== LICH SU GIAO DICH ===");
        if (this.transactionHistory.isEmpty()) {
            System.out.println("Chua co giao dich nao duoc thuc hien.");
        } else {
        
            for (Transaction t : this.transactionHistory) {
                System.out.println("- Loai: " + t.type + " | So tien: " + currencyFormat.format(t.amount) + " | So du sau GD: " +currencyFormat.format( t.balanceAfter));
            }
        }
        System.out.println("=========================");
    }
    //tinh nang 2: Ham doi ma pin
    public void changePin (String newPin){
        this.pin = newPin;
        System.out.println("Doi ma pin thanh cong");
    }
    //Tinh nang 3: Chuyen khoan
    public void transfer(Account targetAccount, double amount) {
        if (amount <= 0) {
            System.out.println("Loi: So tien chuyen phai lon hon 0.");
            return;
        }
        if (this.balance >= amount) {
            this.balance -= amount;
            Transaction tOut = new Transaction("Chuyen tien den STK " + targetAccount.accountNumber, amount, this.balance);
            this.transactionHistory.add(tOut);
            targetAccount.balance += amount;
            Transaction tIn = new Transaction("Nhan tien tu STK " + this.accountNumber, amount, targetAccount.balance);
            targetAccount.transactionHistory.add(tIn);
            
            System.out.println("Chuyen khoan thanh cong: -" + currencyFormat.format(amount));
        } else {
            System.out.println("Giao dich that bai! So du tai khoan khong du.");
        }
    }
}

