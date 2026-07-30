public class Main {
        public static void main(String[] args) {
            DatabaseConnection.getDatabase();
            ATM myATM = new ATM();
            myATM.loadAccounts();
            myATM.start();
        }
}