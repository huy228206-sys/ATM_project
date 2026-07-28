public class Main {
        public static void main(String[] args) {
            ATM myATM = new ATM();
            myATM.loadAccounts();
            myATM.start();
        }
}