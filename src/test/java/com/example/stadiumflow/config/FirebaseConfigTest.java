package com.example.stadiumflow.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class FirebaseConfigTest {

    private FirebaseConfig firebaseConfig;

    @BeforeEach
    public void setup() {
        firebaseConfig = new FirebaseConfig();
        ReflectionTestUtils.setField(firebaseConfig, "projectId", "test-project-id");
    }

    @Test
    public void testFirebaseConfigCreation() {
        assertNotNull(firebaseConfig);
    }

    @Test
    public void testInitialize_DoesNotThrow() {
        // Firebase initialization should not throw even if credentials are missing in test environment
        assertDoesNotThrow(() -> firebaseConfig.initialize());
    }

    @Test
    public void testFirebaseConfigWithNullProjectId() {
        FirebaseConfig config = new FirebaseConfig();
        ReflectionTestUtils.setField(config, "projectId", null);
        assertNotNull(config);
        assertDoesNotThrow(() -> config.initialize());
    }

    @Test
    public void testFirebaseConfigWithEmptyProjectId() {
        FirebaseConfig config = new FirebaseConfig();
        ReflectionTestUtils.setField(config, "projectId", "");
        assertNotNull(config);
        assertDoesNotThrow(() -> config.initialize());
    }

    @Test
    public void testMultipleInitializeCalls() {
        // Multiple calls to initialize should be handled gracefully
        assertDoesNotThrow(() -> {
            firebaseConfig.initialize();
            firebaseConfig.initialize();
        });
    }

    @Test
    public void testFirebaseConfigWithValidProjectId() {
        FirebaseConfig config = new FirebaseConfig();
        ReflectionTestUtils.setField(config, "projectId", "test-project-123");
        assertDoesNotThrow(() -> config.initialize());
    }

    @Test
    public void testFirebaseConfigInitializationDoesNotThrow() {
        assertDoesNotThrow(() -> {
            FirebaseConfig config = new FirebaseConfig();
            ReflectionTestUtils.setField(config, "projectId", "my-test-project");
            config.initialize();
        });
    }

    @Test
    public void testFirebaseConfigWithLongProjectId() {
        FirebaseConfig config = new FirebaseConfig();
        ReflectionTestUtils.setField(config, "projectId", "very-long-project-id-name-for-testing-purposes-123456789");
        assertDoesNotThrow(() -> config.initialize());
    }

    @Test
    public void testFirebaseConfigInitializeMultipleTimes() {
        FirebaseConfig config = new FirebaseConfig();
        ReflectionTestUtils.setField(config, "projectId", "test-project");

        assertDoesNotThrow(() -> {
            config.initialize();
            config.initialize();
            config.initialize();
        });
    }

    @Test
    public void testFirebaseConfigCreationWithoutProjectId() {
        FirebaseConfig config = new FirebaseConfig();
        assertNotNull(config);
        assertDoesNotThrow(() -> config.initialize());
    }
}
