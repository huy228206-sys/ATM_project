import java.util.ArrayList;
import java.util.Scanner;
import java.text.NumberFormat;
import java.util.Locale;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
public class ATM {
    ArrayList<Account> accounts; 
    public ATM() {
        this.accounts = new ArrayList<>();
    }
    // Ham nap tai khoan vao ATM
    public void addAccount(Account acc) {
        this.accounts.add(acc);
    }

    // Ham chay ATM
    public void start() {
        Scanner scanner = new Scanner(System.in);
        int attempts = 0; // bien dem so dang nhap sai
        
        while (true) { 
            System.out.println("\n=== CHAO MUNG DEN VOI JAVABANK ATM ===");
            System.out.print("Nhap so tai khoan (hoac go 'Q' de tat may ATM): ");
            String inputAccNum = scanner.nextLine();
            
            // tinh nang tat chuong trinh (thay cho Thoat)
            if (inputAccNum.equalsIgnoreCase("q")) {
                System.out.println("He thong ATM dang tat... Tam biet!");
                break;
            }
            
            System.out.print("Nhap ma PIN: ");
            String inputPin = scanner.nextLine();

            Account currentAccount = null;
            // Kiem tra dang nhap
            for (Account acc : this.accounts) {
                if (acc.accountNumber.equals(inputAccNum) && acc.pin.equals(inputPin)) {
                    currentAccount = acc; 
                    break; 
                }
            }

            if (currentAccount != null) {
                System.out.println("\nDang nhap thanh cong. Xin chao, " + currentAccount.accountName + "!");
                
                // RESET so lan sai ve 0
                attempts = 0; 
                
                showMenu(currentAccount, scanner);
            } else {
                attempts++; 
                System.out.println("Loi: Sai so tai khoan hoac ma PIN. Ban con " + (3 - attempts) + " lan thu.\n");
                
                // sai 3 lan khoa he thong
                if (attempts >= 3) {
                    System.out.println("Ban da nhap sai qua 3 lan. Khoa the. Chuong trinh ket thuc.");
                    break; 
                }
            }
        }
        scanner.close(); 
    }

    // Ham hien thi Menu 
    public void showMenu(Account acc, Scanner scanner) {
        int choice = 0;
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        while (choice != 7) {
            System.out.println("\n=== Menu Chinh ATM ===");
            System.out.println("1. Kiem Tra So Du");
            System.out.println("2. Nap Tien");
            System.out.println("3. Rut Tien");
            System.out.println("4. Chuyen Khoan:");
            System.out.println("5. Lich Su Giao Dich");
            System.out.println("6.Doi Ma Pin");
            System.out.println("7. Dang Xuat");
            choice = getIntInput(scanner, "Chon mot muc: ");
            
            if (choice == 1) {
                System.out.println("So du hien tai cua ban la: " + currencyFormat.format (acc.balance));

            } else if (choice == 2) {
                double amount = getDoubleInput(scanner, "Nhap so tien muon nap: ");
                acc.deposit(amount);
                saveAccounts();
            }  else if (choice == 3) {
              double amount = getDoubleInput(scanner, "Nhap so tien muon rut: ");
                acc.withdraw(amount);
                saveAccounts();
            } else if (choice == 5) {
                acc.displayHistory();
            } else if (choice == 6) {
                System.out.println("Nhap ma Pin moi cua ban:");
                String newPin = scanner.nextLine();
                acc.changePin(newPin);
                saveAccounts();
            } else if (choice == 7) {
                System.out.println("Dang xuat thanh cong! Đang tro ve man hinh chinh...");

            } else if (choice == 4) { 
                System.out.print("Nhap so tai khoan nguoi nhan: ");
                String targetAccNum = scanner.nextLine();
                
                Account targetAccount = findAccount(targetAccNum);
                saveAccounts();
                if (targetAccount == null) {
                    System.out.println("Loi: Khong tim thay tai khoan dich.");
                } else if (targetAccount.accountNumber.equals(acc.accountNumber)) {
                    System.out.println("Loi: Khong the chuyen khoan cho chinh minh.");
                } else {
                    double amount = getDoubleInput(scanner, "Nhap so tien muon chuyen: ");
                    acc.transfer(targetAccount, amount);
                } } else {
                System.out.println("Loi: Lua chon khong hop le. Vui long chon lai tu 1 toi 7.");
            }
        }
    }
    //Ham chong crash khi nhap sai (nap/rut)
    private double getDoubleInput(Scanner scanner, String prompt) {
        double value = 0;
        boolean valid = false;
        while (!valid) {
            System.out.print(prompt);
            try {
                value = Double.parseDouble(scanner.nextLine());
                valid = true;
            } catch (NumberFormatException e) {
                System.out.println("Loi: Vui long chi nhap so! Hay thu lai.");
            }
        }
        return value;
    }
    //Ham nhap chong crash dung cho menu
    private int getIntInput(Scanner scanner, String prompt) {
        int value = 0;
        boolean valid = false;
        while (!valid) {
            System.out.print(prompt);
            try {
                value = Integer.parseInt(scanner.nextLine());
                valid = true;
            } catch (NumberFormatException e) {
                System.out.println("Loi: Vui long chi nhap so nguyen! Hay thu lai.");
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
    // Ham doc du lieu tu file txt
    public void loadAccounts() {
        File file = new File("accounts.txt");
        if (!file.exists()) {
            System.out.println("Khong tim thay file du lieu. Khoi tao du lieu mac dinh...");
            //tu dong tao 3 tk mac dinh neu k co file
            addAccount(new Account("Huy", "1001", "4321", 500.0));
            addAccount(new Account("Hoa", "1002", "1111", 1000.0));
            addAccount(new Account("Nam", "1003", "9999", 50.0));
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
            System.out.println("Da tai du lieu tu file thanh cong!");
        } catch (Exception e) {
            System.out.println("Loi khi doc file: " + e.getMessage());
        }
    }

    // ham ghi du lieu xuong file txt
    public void saveAccounts() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("accounts.txt"))) {
            for (Account acc : accounts) {
                
                writer.println(acc.accountNumber + "," + acc.accountName + "," + acc.pin + "," + acc.balance);
            }
        } catch (IOException e) {
            System.out.println("Loi khi luu du lieu: " + e.getMessage());
        }
    }
}