import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@SpringBootTest(classes = DatasetTest.class)
@TestPropertySource(locations = "classpath:application.yml")
public class DatasetTest {
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
    public void test() {
        System.out.println("Hello world!");
    }

    // @Test
    // public void connectRedis() {
    //     try(Jedis jedis = new Jedis(redisHost, 6379)) {
    //         String response = jedis.ping();
    //         assertEquals("PONG", response, "Connect to Redis failed");
    //         System.out.println("Redis连接成功");
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     } 
    // }

    @Test
    public void connectMysql() {
        try {
            // 加载MySQL的JDBC驱动
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 建立与数据库的连接
            Connection connection = DriverManager.getConnection(
                    mysqlUrl,
                    mysqlUsername,
                    mysqlPassword
            );

            // 连接成功，输出成功信息
            System.out.println("连接成功");
        } catch (ClassNotFoundException e) {
            // 捕获驱动加载异常
            System.out.println("找不到MySQL的JDBC驱动");
        } catch (SQLException e) {
            // 捕获连接异常
            System.out.println("连接失败");
        }
    }

    @Test
    public void connectMongodb() {
        MongoClient mongoClient = null;
        try {
            mongoClient = MongoClients.create(mongodbUri);
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
