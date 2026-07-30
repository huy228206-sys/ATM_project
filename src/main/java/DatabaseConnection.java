import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class DatabaseConnection {
    private static MongoClient mongoClient = null;
    private static MongoDatabase database = null;

    public static MongoDatabase getDatabase() {
        if (database == null) {
            try {
                mongoClient = MongoClients.create("mongodb://localhost:27017");
                database = mongoClient.getDatabase("atm_db");
                
                System.out.println("-> KET NOI MONGODB THANH CONG!");
            } catch (Exception e) {
                System.out.println("-> KET NOI THAT BAI: " + e.getMessage());
            }
        }
        return database;
    }
}