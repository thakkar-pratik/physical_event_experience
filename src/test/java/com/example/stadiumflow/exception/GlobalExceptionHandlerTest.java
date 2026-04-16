package com.example.stadiumflow.exception;

import com.example.stadiumflow.dto.GenericResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    public void setup() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    public void testHandleValidationErrors() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);

        ResponseEntity<GenericResponse> response = exceptionHandler.handleValidationErrors(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("validation"));
    }

    @Test
    public void testHandleValidationErrors_SingleError() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);

        ResponseEntity<GenericResponse> response = exceptionHandler.handleValidationErrors(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testHandleGlobalErrors() {
        Exception exception = new Exception("Test error message");

        ResponseEntity<GenericResponse> response = exceptionHandler.handleGlobalErrors(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("error", response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("internal system anomaly"));
    }

    @Test
    public void testHandleGlobalErrors_NullMessage() {
        Exception exception = new Exception();

        ResponseEntity<GenericResponse> response = exceptionHandler.handleGlobalErrors(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("internal system anomaly"));
    }

    @Test
    public void testHandleGlobalErrors_RuntimeException() {
        RuntimeException exception = new RuntimeException("Runtime error");

        ResponseEntity<GenericResponse> response = exceptionHandler.handleGlobalErrors(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("internal system anomaly"));
    }
}
