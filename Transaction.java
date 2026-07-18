public class Transaction {
    String type;
    double amount;
    double balanceAfter;
    public Transaction(String type, double amount, double after){
        this.type = type;
        this.amount = amount;
        balanceAfter = after;

    }
}
