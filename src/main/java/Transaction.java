import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class Transaction {
    String type;
    double amount;
    double balanceAfter;

    public Transaction(String type, double amount, double after) {
        this.type = type;
        this.amount = amount;
        balanceAfter = after;
    }

    public void saveToDatabase(String accountNumber){
        try{
            MongoDatabase database = DatabaseConnection.getDatabase();
            MongoCollection<Document> collection = database.getCollection("transactions");
            Document doc = new Document()
                    .append("accountNumber", accountNumber)
                    .append("type", this.type)
                    .append("amount", this.amount)
                    .append("balanceAfter", this.balanceAfter)
                    .append("timestamp", new java.util.Date());
                    
            collection.insertOne(doc);
        }catch (Exception e){
            System.out.println("-> Loi khi luu giao dich: " + e.getMessage());
        }
    }
}