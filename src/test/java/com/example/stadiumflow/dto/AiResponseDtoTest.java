package com.example.stadiumflow.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AiResponseDtoTest {

    @Test
    public void testDefaultConstructor() {
        AiResponseDto dto = new AiResponseDto();
        
        assertNotNull(dto);
        assertNull(dto.getResponse());
        assertNull(dto.getProvider());
    }

    @Test
    public void testParameterizedConstructor() {
        AiResponseDto dto = new AiResponseDto("Test response", "Test provider");
        
        assertNotNull(dto);
        assertEquals("Test response", dto.getResponse());
        assertEquals("Test provider", dto.getProvider());
    }

    @Test
    public void testSetters() {
        AiResponseDto dto = new AiResponseDto();
        
        dto.setResponse("New response");
        dto.setProvider("New provider");
        
        assertEquals("New response", dto.getResponse());
        assertEquals("New provider", dto.getProvider());
    }

    @Test
    public void testGettersAfterConstructor() {
        AiResponseDto dto = new AiResponseDto("Response text", "Provider name");
        
        assertEquals("Response text", dto.getResponse());
        assertEquals("Provider name", dto.getProvider());
    }

    @Test
    public void testNullValues() {
        AiResponseDto dto = new AiResponseDto(null, null);
        
        assertNull(dto.getResponse());
        assertNull(dto.getProvider());
    }

    @Test
    public void testEmptyStrings() {
        AiResponseDto dto = new AiResponseDto("", "");
        
        assertEquals("", dto.getResponse());
        assertEquals("", dto.getProvider());
    }

    @Test
    public void testLongStrings() {
        String longResponse = "Very long response ".repeat(100);
        String longProvider = "Long provider ".repeat(50);
        
        AiResponseDto dto = new AiResponseDto(longResponse, longProvider);
        
        assertEquals(longResponse, dto.getResponse());
        assertEquals(longProvider, dto.getProvider());
    }

    @Test
    public void testSpecialCharacters() {
        AiResponseDto dto = new AiResponseDto("Response!@#$%^&*()", "Provider<>{}[]");
        
        assertEquals("Response!@#$%^&*()", dto.getResponse());
        assertEquals("Provider<>{}[]", dto.getProvider());
    }
}
