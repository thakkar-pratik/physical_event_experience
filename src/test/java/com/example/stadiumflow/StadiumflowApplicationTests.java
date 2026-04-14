package com.example.stadiumflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class StadiumflowApplicationTests {

    @Test
    public void contextLoads() {
        // Just tests whether the Spring application context can successfully load.
    }
    
    @Test
    public void testMain() {
        // Start app with a custom property to avoid server port clashing or let it run briefly
        StadiumflowApplication.main(new String[]{"--server.port=8081"});
    }
}
