import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerTest {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    @Test
    public void testServerIsRunning() {
        try (Socket socket = new Socket(HOST, PORT)) {
            assertTrue(socket.isConnected());
        } catch (IOException e) {
            // 如果连接失败，那么测试失败
            assertTrue(false);
        }
    }
}