package com.example.stadiumflow.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Advanced tests for FirebaseConfig to cover all initialization paths
 */
public class FirebaseConfigAdvancedTest {

    private FirebaseConfig firebaseConfig;

    @BeforeEach
    public void setup() {
        firebaseConfig = new FirebaseConfig();
        ReflectionTestUtils.setField(firebaseConfig, "projectId", "test-project");
    }

    @Test
    public void testInitialize_WhenFirebaseAppsEmpty_SuccessfulInit() {
        try (MockedStatic<FirebaseApp> mockedFirebaseApp = mockStatic(FirebaseApp.class);
             MockedStatic<GoogleCredentials> mockedCredentials = mockStatic(GoogleCredentials.class);
             MockedStatic<FirebaseOptions> mockedOptions = mockStatic(FirebaseOptions.class)) {
            
            // Mock FirebaseApp.getApps() to return empty list
            mockedFirebaseApp.when(FirebaseApp::getApps).thenReturn(new ArrayList<>());
            
            // Mock GoogleCredentials
            GoogleCredentials mockCreds = mock(GoogleCredentials.class);
            mockedCredentials.when(GoogleCredentials::getApplicationDefault).thenReturn(mockCreds);
            
            // Mock FirebaseOptions builder
            FirebaseOptions.Builder mockBuilder = mock(FirebaseOptions.Builder.class);
            when(mockBuilder.setCredentials(any())).thenReturn(mockBuilder);
            when(mockBuilder.setProjectId(any())).thenReturn(mockBuilder);
            
            FirebaseOptions mockOptions = mock(FirebaseOptions.class);
            when(mockBuilder.build()).thenReturn(mockOptions);
            
            mockedOptions.when(FirebaseOptions::builder).thenReturn(mockBuilder);
            
            // Mock FirebaseApp initialization
            FirebaseApp mockApp = mock(FirebaseApp.class);
            mockedFirebaseApp.when(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class))).thenReturn(mockApp);
            
            // Execute
            assertDoesNotThrow(() -> firebaseConfig.initialize());
            
            // Verify initialization was called
            mockedFirebaseApp.verify(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class)), times(1));
        }
    }

    @Test
    public void testInitialize_WhenFirebaseAlreadyInitialized() {
        try (MockedStatic<FirebaseApp> mockedFirebaseApp = mockStatic(FirebaseApp.class)) {
            
            // Mock FirebaseApp.getApps() to return non-empty list
            FirebaseApp existingApp = mock(FirebaseApp.class);
            mockedFirebaseApp.when(FirebaseApp::getApps).thenReturn(Arrays.asList(existingApp));
            
            // Execute
            assertDoesNotThrow(() -> firebaseConfig.initialize());
            
            // Verify initialization was NOT called
            mockedFirebaseApp.verify(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class)), never());
        }
    }

    @Test
    public void testInitialize_WhenIOExceptionThrown() {
        try (MockedStatic<FirebaseApp> mockedFirebaseApp = mockStatic(FirebaseApp.class);
             MockedStatic<GoogleCredentials> mockedCredentials = mockStatic(GoogleCredentials.class)) {
            
            // Mock FirebaseApp.getApps() to return empty list
            mockedFirebaseApp.when(FirebaseApp::getApps).thenReturn(new ArrayList<>());
            
            // Mock GoogleCredentials to throw IOException
            mockedCredentials.when(GoogleCredentials::getApplicationDefault)
                .thenThrow(new IOException("Credentials not found"));
            
            // Execute - should not throw, just log warning
            assertDoesNotThrow(() -> firebaseConfig.initialize());
            
            // Verify initialization was not completed
            mockedFirebaseApp.verify(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class)), never());
        }
    }

    @Test
    public void testInitialize_WithNullProjectId() {
        ReflectionTestUtils.setField(firebaseConfig, "projectId", null);
        
        try (MockedStatic<FirebaseApp> mockedFirebaseApp = mockStatic(FirebaseApp.class);
             MockedStatic<GoogleCredentials> mockedCredentials = mockStatic(GoogleCredentials.class);
             MockedStatic<FirebaseOptions> mockedOptions = mockStatic(FirebaseOptions.class)) {
            
            mockedFirebaseApp.when(FirebaseApp::getApps).thenReturn(new ArrayList<>());
            
            GoogleCredentials mockCreds = mock(GoogleCredentials.class);
            mockedCredentials.when(GoogleCredentials::getApplicationDefault).thenReturn(mockCreds);
            
            FirebaseOptions.Builder mockBuilder = mock(FirebaseOptions.Builder.class);
            when(mockBuilder.setCredentials(any())).thenReturn(mockBuilder);
            when(mockBuilder.setProjectId(null)).thenReturn(mockBuilder);
            
            FirebaseOptions mockOptions = mock(FirebaseOptions.class);
            when(mockBuilder.build()).thenReturn(mockOptions);
            
            mockedOptions.when(FirebaseOptions::builder).thenReturn(mockBuilder);
            
            FirebaseApp mockApp = mock(FirebaseApp.class);
            mockedFirebaseApp.when(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class))).thenReturn(mockApp);
            
            assertDoesNotThrow(() -> firebaseConfig.initialize());
        }
    }

    @Test
    public void testInitialize_MultipleCallsWithExistingApp() {
        try (MockedStatic<FirebaseApp> mockedFirebaseApp = mockStatic(FirebaseApp.class)) {
            
            FirebaseApp existingApp = mock(FirebaseApp.class);
            mockedFirebaseApp.when(FirebaseApp::getApps).thenReturn(Arrays.asList(existingApp));
            
            // Call multiple times
            firebaseConfig.initialize();
            firebaseConfig.initialize();
            firebaseConfig.initialize();
            
            // Should never try to initialize
            mockedFirebaseApp.verify(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class)), never());
        }
    }
}
