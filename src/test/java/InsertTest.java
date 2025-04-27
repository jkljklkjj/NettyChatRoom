import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.model.mongo.MongoGroup;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//@SpringBootTest(classes = InsertTest.class)
@TestPropertySource(locations = "classpath:application.yml")
public class InsertTest {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.datasource.url}")
    private String mysqlUrl;

    @Value("${spring.datasource.username}")
    private String mysqlUsername;

    @Value("${spring.datasource.password}")
    private String mysqlPassword;

    @Value("${spring.data.mongodb.uri}")
    private String mongodbUri;

    @Test
    public void insertMonggo() {
        MongoClient mongoClient = MongoClients.create("mongodb://39.104.61.4:27017");

        // Select the database
        MongoDatabase database = mongoClient.getDatabase("test");

        // List all collections in the database
        for (String name : database.listCollectionNames()) {
            System.out.println(name);
            MongoCollection<Document> collection = database.getCollection(name);
            System.out.println(collection.countDocuments());
        }

        // Close the connection
        mongoClient.close();
    }
}
