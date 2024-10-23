import com.example.Application;
import com.example.controller.UserController;
import com.example.model.mongo.MongoUser;
import com.example.model.mysql.User;
import com.example.service.mongo.MongoUserService;
import com.example.service.mysql.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
public class UserTest {

    private MockMvc mockMvc;
    private String token;

    @Mock
    private UserService userService;

    @Mock
    private MongoUserService mongoUserService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void print() {
        System.out.println("用户业务测试类，启动！");
    }

    @Test
    public void testRegisterUser() throws Exception {
        when(userService.register(any(User.class))).thenReturn(1);
        when(mongoUserService.register(any(MongoUser.class))).thenReturn(true);

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testUser\"," +
                                "\"password\":\"password\"," +
                                "\"email\":\"2998568539@qq.com\"," +
                                "\"phone\":\"12345678901\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testLoginUser() throws Exception {
        int id = 1;
        String password = "123456";

        when(userService.authenticateUser(id, password)).thenReturn(true);

        MvcResult result = mockMvc.perform(get("/user/login")
                        .param("id", String.valueOf(id))
                        .param("password", password))
                .andExpect(status().isOk())
                .andReturn();

        token = result.getResponse().getContentAsString();
        System.out.println(token);
    }
}