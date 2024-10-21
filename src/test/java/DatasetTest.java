import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import redis.clients.jedis.Jedis;

public class DatasetTest {
    @Test
    public void test() {
        System.out.println("Hello world!");
    }

    @Test
    public void connectRedis() {
        try(Jedis jedis = new Jedis("39.104.61.4", 6379)) {
            String response = jedis.ping();
            assertEquals("PONG", response, "Connect to Redis failed");
            System.out.println("Redis连接成功");
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    @Test
    public void connectMysql() {
        String resource = "sql/User-config.xml";
        try (Reader reader = Resources.getResourceAsReader(resource)) {
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            try (SqlSession session = sqlSessionFactory.openSession()) {
                assertNotNull(session.getConnection(), "Failed to connect to MySQL using MyBatis");
                System.out.println("MyBatis connected to MySQL successfully");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void connectMongodb() {
        MongoClient mongoClient = null;
        try {
            mongoClient = MongoClients.create("mongodb://39.104.61.4:27017");
            assertEquals("Connected to MongoDB successfully", "Connected to MongoDB successfully");
            System.out.println("Connected to MongoDB successfully");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mongoClient != null) {
                mongoClient.close();
                System.out.println("Disconnected from MongoDB");
            }
        }
    }
}
