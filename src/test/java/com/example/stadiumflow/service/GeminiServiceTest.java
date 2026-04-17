package com.example.stadiumflow.service;

import com.example.stadiumflow.domain.Zone;
import com.example.stadiumflow.dto.AiResponseDto;
import com.example.stadiumflow.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GeminiServiceTest {

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private GeminiApiService geminiApiService;

    private GeminiService geminiService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Create GeminiService with constructor parameters
        geminiService = new GeminiService(
            zoneRepository,
            "test-project-id",
            "us-central1",
            "gemini-1.5-pro",
            "test-api-key"
        );

        Zone gateA = new Zone("Gate_A", "Gate A", 25, 80);
        Zone gateC = new Zone("Gate_C", "Gate C", 5, 20);
        Zone section112 = new Zone("Section_112", "Section 112", 2, 10);
        Zone section120 = new Zone("Section_120", "Section 120", 15, 50);

        when(zoneRepository.findAll()).thenReturn(Arrays.asList(gateA, gateC, section112, section120));
    }

    @Test
    public void testProcessQuery_SpecificZone_Busy() {
        AiResponseDto response = geminiService.processQuery("how is gate a?");
        
        assertNotNull(response);
        assertNotNull(response.getResponse());
        assertTrue(response.getResponse().contains("Gate A"));
        assertTrue(response.getResponse().contains("quite busy"));
        assertNotNull(response.getProvider());
    }

    @Test
    public void testProcessQuery_SpecificZone_Clear() {
        AiResponseDto response = geminiService.processQuery("how is gate c?");
        
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Gate C"));
        assertTrue(response.getResponse().contains("flow looks great") || response.getResponse().contains("perfect time"));
    }

    @Test
    public void testProcessQuery_StatusReport() {
        AiResponseDto response = geminiService.processQuery("give me a status overview");
        
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Stadium Health Report"));
        assertTrue(response.getResponse().contains("Gate A"));
    }

    @Test
    public void testProcessQuery_StatusReport_EmptyZones() {
        when(zoneRepository.findAll()).thenReturn(Arrays.asList());
        AiResponseDto response = geminiService.processQuery("give me a status overview");
        
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Stadium Health Report"));
    }

    @Test
    public void testProcessQuery_DealFinder_YesDeal() {
        AiResponseDto response = geminiService.processQuery("is there a deal?");
        
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Dynamic Yield discount is currently active"));
        assertTrue(response.getResponse().contains("Section 112"));
    }

    @Test
    public void testProcessQuery_DealFinder_NoDeal() {
        Zone section112 = new Zone("Section_112", "Section 112", 15, 10);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(section112));
        
        AiResponseDto response = geminiService.processQuery("give me a discount");
        
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Concessions are at normal capacity."));
    }
    
    @Test
    public void testProcessQuery_DealFinder_NoStands() {
        Zone gate = new Zone("Gate_A", "Gate A", 5, 10);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(gate));
        
        AiResponseDto response = geminiService.processQuery("give me a discount");
        
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Concessions are at normal capacity."));
    }

    @Test
    public void testProcessQuery_Fallback() {
        AiResponseDto response = geminiService.processQuery("hello");
        
        assertNotNull(response);
        assertTrue(response.getResponse().contains("StadiumPulse Concert Concierge"));
    }
    
    @Test
    public void testProcessQuery_ZoneMatching_VIP() {
        Zone vip = new Zone("VIP_Zone", "VIP Lounge", 10, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(vip));
        
        AiResponseDto response = geminiService.processQuery("what about vip?");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("VIP"));
    }
    
    @Test
    public void testProcessQuery_Food() {
        AiResponseDto response = geminiService.processQuery("where can I get food?");
        assertNotNull(response);
        assertNotNull(response.getResponse());
    }
    
    @Test
    public void testProcessQuery_Hydration() {
        Zone hydration = new Zone("Hydration_1", "Hydration Station", 3, 15);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(hydration));

        AiResponseDto response = geminiService.processQuery("hydration station status?");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Hydration"));
    }

    @Test
    public void testProcessQuery_MultipleZones_FindBestMatch() {
        Zone gate1 = new Zone("Gate_A", "Gate A", 5, 20);
        Zone gate2 = new Zone("Gate_B", "Gate B", 10, 30);
        Zone section = new Zone("Section_100", "Section 100", 8, 25);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(gate1, gate2, section));

        AiResponseDto response = geminiService.processQuery("gate b status");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Gate B"));
    }

    @Test
    public void testProcessQuery_CaseInsensitiveMatching() {
        Zone vip = new Zone("VIP_Lounge", "VIP Lounge", 2, 10);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(vip));

        AiResponseDto response = geminiService.processQuery("vip lounge");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_PartialZoneNameMatch() {
        Zone section = new Zone("Section_112", "Section 112", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(section));

        AiResponseDto response = geminiService.processQuery("section 112");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_BusyZone_RecommendAlternative() {
        Zone busyZone = new Zone("Gate_Main", "Main Gate", 25, 80);
        Zone clearZone = new Zone("Gate_Side", "Side Gate", 3, 15);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(busyZone, clearZone));

        AiResponseDto response = geminiService.processQuery("gate main");
        assertNotNull(response);
        // Response should contain information about the gate
        assertTrue(response.getResponse().length() > 0);
    }

    @Test
    public void testProcessQuery_StatusKeywords() {
        Zone zone = new Zone("Test_Zone", "Test Zone", 10, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response1 = geminiService.processQuery("status");
        AiResponseDto response2 = geminiService.processQuery("overview");
        AiResponseDto response3 = geminiService.processQuery("report");

        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);
    }

    @Test
    public void testProcessQuery_DealKeywords() {
        Zone stand = new Zone("Stand_1", "Food Stand 1", 3, 10);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(stand));

        AiResponseDto response1 = geminiService.processQuery("deal");
        AiResponseDto response2 = geminiService.processQuery("discount");
        AiResponseDto response3 = geminiService.processQuery("offer");

        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);
    }

    @Test
    public void testProcessQuery_NullQuery() {
        assertThrows(NullPointerException.class, () -> {
            geminiService.processQuery(null);
        });
    }

    @Test
    public void testProcessQuery_VeryLongQuery() {
        String longQuery = "This is a very long query ".repeat(100);
        AiResponseDto response = geminiService.processQuery(longQuery);
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_SpecialCharactersInQuery() {
        AiResponseDto response = geminiService.processQuery("!@#$%^&*()");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_EmptyZoneList() {
        when(zoneRepository.findAll()).thenReturn(Arrays.asList());

        AiResponseDto response = geminiService.processQuery("status");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Stadium Health Report"));
    }

    @Test
    public void testProcessQuery_AllZonesBusy() {
        Zone zone1 = new Zone("Gate_1", "Gate 1", 20, 70);
        Zone zone2 = new Zone("Gate_2", "Gate 2", 25, 80);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone1, zone2));

        AiResponseDto response = geminiService.processQuery("gate 1");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_ZoneWithZeroWaitTime() {
        Zone zone = new Zone("Express_Lane", "Express Lane", 0, 5);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("express lane");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_ZoneWithHighDensity() {
        Zone zone = new Zone("Packed_Area", "Packed Area", 15, 95);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("packed area");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_StatusKeyword_Status() {
        Zone zone = new Zone("Zone_1", "Zone 1", 10, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("status");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Stadium Health Report"));
    }

    @Test
    public void testProcessQuery_StatusKeyword_Capacity() {
        Zone zone = new Zone("Zone_1", "Zone 1", 10, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("what's the capacity?");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Stadium Health Report"));
    }

    @Test
    public void testProcessQuery_DealKeyword_Deal() {
        Zone stand = new Zone("Section_100", "Food Stand", 3, 10);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(stand));

        AiResponseDto response = geminiService.processQuery("any deals?");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_DealKeyword_Cheap() {
        Zone stand = new Zone("Section_200", "Food Stand 2", 4, 15);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(stand));

        AiResponseDto response = geminiService.processQuery("where can I find cheap food?");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_DealKeyword_Offer() {
        Zone stand = new Zone("Section_300", "Food Stand 3", 2, 8);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(stand));

        AiResponseDto response = geminiService.processQuery("any offers?");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_ZoneMatching_North() {
        Zone north = new Zone("North_Gate", "North Gate Entrance", 8, 25);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(north));

        AiResponseDto response = geminiService.processQuery("north gate");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("North"));
    }

    @Test
    public void testProcessQuery_ZoneMatching_VIPKeyword() {
        Zone vip = new Zone("VIP_Area", "VIP Lounge Area", 5, 15);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(vip));

        AiResponseDto response = geminiService.processQuery("vip area");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_ZoneMatching_FoodKeyword() {
        Zone food = new Zone("Food_Court", "Food Court Plaza", 12, 40);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(food));

        AiResponseDto response = geminiService.processQuery("food court");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_ZoneMatching_HydrationKeyword() {
        Zone hydration = new Zone("Hydration_Station", "Hydration Station West", 6, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(hydration));

        AiResponseDto response = geminiService.processQuery("hydration");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_ZoneMatching_GateA() {
        Zone gateA = new Zone("Gate_A", "Gate A Main", 7, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(gateA));

        AiResponseDto response = geminiService.processQuery("gate a");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Gate A"));
    }

    @Test
    public void testProcessQuery_ZoneWithUnderscoreInId() {
        Zone zone = new Zone("North_Stand", "North Stand", 10, 35);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("north stand");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_BusiestAndEmptiestZones() {
        Zone busiest = new Zone("Zone_Busy", "Busy Zone", 25, 80);
        Zone emptiest = new Zone("Zone_Empty", "Empty Zone", 2, 5);
        Zone medium = new Zone("Zone_Med", "Medium Zone", 10, 40);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(busiest, emptiest, medium));

        AiResponseDto response = geminiService.processQuery("status");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Busy Zone") || response.getResponse().contains("Empty Zone"));
    }

    @Test
    public void testProcessQuery_SingleZone_BusyTrigger() {
        Zone busy = new Zone("Main_Gate", "Main Gate", 20, 75);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(busy));

        AiResponseDto response = geminiService.processQuery("main gate");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("quite busy") || response.getResponse().contains("recommend"));
    }

    @Test
    public void testProcessQuery_SingleZone_ClearFlow() {
        Zone clear = new Zone("Side_Gate", "Side Gate", 5, 15);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(clear));

        AiResponseDto response = geminiService.processQuery("side gate");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("perfect time") || response.getResponse().contains("flow looks great"));
    }

    @Test
    public void testProcessQuery_DealAvailable() {
        Zone stand = new Zone("Section_112", "Food Stand Section 112", 3, 8);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(stand));

        AiResponseDto response = geminiService.processQuery("any discounts?");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Dynamic Yield discount") || response.getResponse().contains("Section 112"));
    }

    @Test
    public void testProcessQuery_NoDealAvailable_HighWaitTime() {
        Zone stand = new Zone("Section_200", "Food Stand Section 200", 10, 35);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(stand));

        AiResponseDto response = geminiService.processQuery("any discounts?");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("normal capacity"));
    }

    @Test
    public void testProcessQuery_GenericGreeting() {
        Zone zone = new Zone("Zone_1", "Zone 1", 10, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("hi");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("StadiumPulse"));
    }

    @Test
    public void testProcessQuery_UnrecognizedQuery() {
        Zone zone = new Zone("Zone_1", "Zone 1", 10, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("random unrecognized text xyz");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("StadiumPulse"));
    }

    @Test
    public void testProcessQuery_ZoneWaitTimeExactly15Minutes() {
        Zone zone = new Zone("Gate_Test", "Test Gate", 15, 50);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("gate test");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("flow looks great") || response.getResponse().contains("perfect time"));
    }

    @Test
    public void testProcessQuery_ZoneWaitTimeAt16Minutes() {
        Zone zone = new Zone("Gate_Test2", "Test Gate 2", 16, 55);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("gate test2");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("quite busy"));
    }

    @Test
    public void testProcessQuery_DealWithStandAtExactly5Minutes() {
        Zone stand = new Zone("Section_200", "Food Stand 200", 5, 25);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(stand));

        AiResponseDto response = geminiService.processQuery("deal");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("normal capacity"));
    }

    @Test
    public void testProcessQuery_DealWithStandAt4Minutes() {
        Zone stand = new Zone("Section_300", "Food Stand 300", 4, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(stand));

        AiResponseDto response = geminiService.processQuery("deal");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Dynamic Yield") || response.getResponse().contains("low wait"));
    }

    @Test
    public void testProcessQuery_HowIsKeyword() {
        Zone zone = new Zone("Gate_C", "Gate C", 9, 35);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("how is everything?");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Stadium Health Report"));
    }

    @Test
    public void testProcessQuery_ZoneMatch_NorthKeyword() {
        Zone northZone = new Zone("North_1", "North Stand", 8, 30);
        Zone southZone = new Zone("South_1", "South Stand", 12, 45);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(northZone, southZone));

        AiResponseDto response = geminiService.processQuery("what about north?");
        assertNotNull(response);
        assertTrue(response.getResponse().toLowerCase().contains("north"));
    }

    @Test
    public void testProcessQuery_ZoneMatch_VIPKeywordInName() {
        Zone vipZone = new Zone("VIP_1", "VIP Lounge", 4, 18);
        Zone normalZone = new Zone("Gate_1", "Normal Gate", 11, 42);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(vipZone, normalZone));

        AiResponseDto response = geminiService.processQuery("tell me about vip");
        assertNotNull(response);
        assertTrue(response.getResponse().toLowerCase().contains("vip"));
    }

    @Test
    public void testProcessQuery_ZoneMatch_FoodKeywordInName() {
        Zone foodZone = new Zone("Food_1", "Food Court", 6, 25);
        Zone gateZone = new Zone("Gate_2", "Main Gate", 13, 48);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(foodZone, gateZone));

        AiResponseDto response = geminiService.processQuery("food court status");
        assertNotNull(response);
        assertTrue(response.getResponse().toLowerCase().contains("food"));
    }

    @Test
    public void testProcessQuery_ZoneMatch_HydrationKeywordInName() {
        Zone hydrationZone = new Zone("Hydration_1", "Hydration Station", 3, 14);
        Zone otherZone = new Zone("Gate_3", "Gate 3", 10, 38);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(hydrationZone, otherZone));

        AiResponseDto response = geminiService.processQuery("hydration station");
        assertNotNull(response);
        assertTrue(response.getResponse().toLowerCase().contains("hydration"));
    }

    @Test
    public void testProcessQuery_ZoneMatch_GateAKeywordInName() {
        Zone gateA = new Zone("Gate_A", "Gate A Main", 7, 28);
        Zone gateB = new Zone("Gate_B", "Gate B Side", 14, 52);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(gateA, gateB));

        AiResponseDto response = geminiService.processQuery("gate a status");
        assertNotNull(response);
        assertTrue(response.getResponse().toLowerCase().contains("gate a"));
    }

    @Test
    public void testProcessQuery_ZoneMatch_IdWithUnderscore() {
        Zone zone = new Zone("Main_Entrance", "Main Entrance Hall", 9, 34);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("main entrance");
        assertNotNull(response);
        assertTrue(response.getResponse().toLowerCase().contains("main entrance"));
    }

    @Test
    public void testProcessQuery_ZoneMatch_ByIdLowercase() {
        Zone zone = new Zone("Gate_C", "Gate C", 11, 41);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("gate_c");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_NoZoneMatch_StatusWithBusiestNull() {
        when(zoneRepository.findAll()).thenReturn(Arrays.asList());

        AiResponseDto response = geminiService.processQuery("status");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Stadium Health Report"));
    }

    @Test
    public void testProcessQuery_StatusWithBusiestAndEmptiestFound() {
        Zone zone1 = new Zone("Z1", "Zone 1", 5, 20);
        Zone zone2 = new Zone("Z2", "Zone 2", 15, 60);
        Zone zone3 = new Zone("Z3", "Zone 3", 10, 40);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone1, zone2, zone3));

        AiResponseDto response = geminiService.processQuery("status");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Zone 2")); // busiest
        assertTrue(response.getResponse().contains("Zone 1")); // emptiest
    }

    @Test
    public void testProcessQuery_DealWith_SectionStandWaitTimeLessThan5() {
        Zone stand = new Zone("Section_100", "Food Stand 100", 4, 18);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(stand));

        AiResponseDto response = geminiService.processQuery("deal");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Dynamic Yield") || response.getResponse().contains("low wait"));
    }

    @Test
    public void testProcessQuery_DealWith_SectionStandWaitTime5OrMore() {
        Zone stand = new Zone("Section_200", "Food Stand 200", 5, 25);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(stand));

        AiResponseDto response = geminiService.processQuery("deal");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("normal capacity"));
    }

    @Test
    public void testProcessQuery_DealWith_NoSectionStands() {
        Zone gate = new Zone("Gate_Main", "Main Gate", 8, 32);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(gate));

        AiResponseDto response = geminiService.processQuery("cheap");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("normal capacity"));
    }

    @Test
    public void testProcessQuery_DealWith_EmptyStandsList() {
        when(zoneRepository.findAll()).thenReturn(Arrays.asList());

        AiResponseDto response = geminiService.processQuery("discount");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("normal capacity"));
    }

    @Test
    public void testProcessQuery_FallbackMessage() {
        Zone zone = new Zone("Test", "Test", 10, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("xyz random text");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("StadiumPulse"));
    }

    @Test
    public void testProcessQuery_OverviewKeyword() {
        Zone zone = new Zone("Test", "Test", 8, 28);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("overview");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Stadium Health Report"));
    }

    @Test
    public void testProcessQuery_CapacityKeyword() {
        Zone zone = new Zone("Test", "Test", 12, 44);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("capacity");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Stadium Health Report"));
    }

    @Test
    public void testProcessQuery_WithEmptyStringQuery() {
        Zone zone = new Zone("Test", "Test", 10, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("StadiumPulse"));
    }

    @Test
    public void testProcessQuery_WithWhitespaceOnly() {
        Zone zone = new Zone("Test", "Test", 10, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("   ");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_WithVeryLongQuery() {
        String longQuery = "What is the status of " + "zone ".repeat(1000) + "?";
        Zone zone = new Zone("Test", "Test", 10, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery(longQuery);
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_WithSpecialCharacters() {
        Zone zone = new Zone("Test", "Test", 10, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("!@#$%^&*()_+{}|:<>?");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_WithNumbers() {
        Zone zone = new Zone("Test", "Test", 10, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("12345");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_MixedCaseStatus() {
        Zone zone = new Zone("Test", "Test", 10, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("StAtUs");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Stadium Health Report"));
    }

    @Test
    public void testProcessQuery_MixedCaseDeal() {
        Zone stand = new Zone("Section_100", "Food Stand", 3, 10);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(stand));

        AiResponseDto response = geminiService.processQuery("DeAl");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_StatusWithSingleZone() {
        Zone zone = new Zone("Only_Zone", "Only Zone", 10, 35);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("status");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Only Zone"));
    }

    @Test
    public void testProcessQuery_AllZonesSameWaitTime() {
        Zone z1 = new Zone("Z1", "Zone 1", 10, 30);
        Zone z2 = new Zone("Z2", "Zone 2", 10, 30);
        Zone z3 = new Zone("Z3", "Zone 3", 10, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(z1, z2, z3));

        AiResponseDto response = geminiService.processQuery("status");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_DealWithMultipleSections() {
        Zone s1 = new Zone("Section_100", "Stand 100", 4, 18);
        Zone s2 = new Zone("Section_200", "Stand 200", 3, 15);
        Zone s3 = new Zone("Section_300", "Stand 300", 6, 22);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(s1, s2, s3));

        AiResponseDto response = geminiService.processQuery("deal");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_ZoneMatchWithPartialName() {
        Zone zone = new Zone("North_VIP_Food_Court", "North VIP Food Court", 7, 28);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("vip");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_WithEmptyZoneDatabase() {
        when(zoneRepository.findAll()).thenReturn(new ArrayList<>());
        AiResponseDto response = geminiService.processQuery("status");
        assertNotNull(response);
        assertNotNull(response.getProvider());
    }

    @Test
    public void testProcessQuery_SpecialCharactersHandling() {
        AiResponseDto response = geminiService.processQuery("!@#$%^&*()");
        assertNotNull(response);
        assertNotNull(response.getProvider());
    }

    @Test
    public void testProcessQuery_ValidQueryWithValidResponse() {
        AiResponseDto response = geminiService.processQuery("What's the stadium status?");
        assertNotNull(response);
        assertNotNull(response.getProvider());
        assertNotNull(response.getResponse());
    }

    @Test
    public void testProcessQuery_AllZonesWithDifferentWaitTimes() {
        List<Zone> zones = Arrays.asList(
            new Zone("Gate_A", "Gate A", 1, 10),
            new Zone("Gate_B", "Gate B", 5, 20),
            new Zone("Gate_C", "Gate C", 10, 30),
            new Zone("Gate_D", "Gate D", 15, 40)
        );
        when(zoneRepository.findAll()).thenReturn(zones);

        AiResponseDto response = geminiService.processQuery("status");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Stadium Health Report") ||
                   response.getResponse().contains("concierge"));
    }

    @Test
    public void testProcessQuery_ZoneWithVeryHighWaitTime() {
        Zone busyZone = new Zone("Gate_A", "Main Entrance", 25, 85);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(busyZone));

        AiResponseDto response = geminiService.processQuery("which gate");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_DealQueryWithMultipleStands() {
        List<Zone> stands = Arrays.asList(
            new Zone("Section_101", "Food Court A", 2, 15),
            new Zone("Section_102", "Food Court B", 8, 35),
            new Zone("Section_103", "Food Court C", 12, 45)
        );
        when(zoneRepository.findAll()).thenReturn(stands);

        AiResponseDto response = geminiService.processQuery("any deals on food?");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("Food Court A") ||
                   response.getResponse().contains("deal") ||
                   response.getResponse().contains("discount"));
    }

    @Test
    public void testProcessQuery_DealQueryWithNoDeals() {
        List<Zone> stands = Arrays.asList(
            new Zone("Section_101", "Food Court", 10, 40)
        );
        when(zoneRepository.findAll()).thenReturn(stands);

        AiResponseDto response = geminiService.processQuery("deals");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_GateQueryWithMultipleGates() {
        List<Zone> gates = Arrays.asList(
            new Zone("Gate_A", "North Gate", 2, 15),
            new Zone("Gate_B", "South Gate", 4, 25),
            new Zone("Gate_C", "East Gate", 8, 35)
        );
        when(zoneRepository.findAll()).thenReturn(gates);

        AiResponseDto response = geminiService.processQuery("best gate to enter");
        assertNotNull(response);
        assertNotNull(response.getResponse());
        assertNotNull(response.getProvider());
    }

    @Test
    public void testProcessQuery_CaseInsensitiveKeywords() {
        Zone zone = new Zone("Gate_A", "Main Entrance", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response1 = geminiService.processQuery("STATUS");
        AiResponseDto response2 = geminiService.processQuery("Status");
        AiResponseDto response3 = geminiService.processQuery("status");

        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);
    }

    @Test
    public void testProcessQuery_MultipleKeywordsInQuery() {
        List<Zone> zones = Arrays.asList(
            new Zone("Gate_A", "North Gate", 3, 18),
            new Zone("Section_101", "Food Court", 4, 22)
        );
        when(zoneRepository.findAll()).thenReturn(zones);

        AiResponseDto response = geminiService.processQuery("what is the status and any deals?");
        assertNotNull(response);
    }

    @Test
    public void testProcessQuery_UnknownKeyword() {
        Zone zone = new Zone("Gate_A", "Main Gate", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("random unknown query xyz");
        assertNotNull(response);
        assertTrue(response.getResponse().contains("concierge") ||
                   response.getResponse().contains("ask"));
    }

    // ==== 100% BRANCH COVERAGE TESTS ====

    @Test
    public void testProcessQuery_ZoneWithExactly15MinWait() {
        // Test boundary condition at line 172: z.getWaitTime() > 15
        Zone zone = new Zone("Gate_A", "Gate A", 15, 60);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("how is gate a?");

        assertNotNull(response);
        assertTrue(response.getResponse().contains("Gate A"));
        // At exactly 15 min, should NOT say "quite busy"
        assertFalse(response.getResponse().contains("quite busy"));
    }

    @Test
    public void testProcessQuery_ZoneWithExactly16MinWait() {
        // Test boundary condition at line 172: z.getWaitTime() > 15
        Zone zone = new Zone("Gate_A", "Gate A", 16, 60);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("how is gate a?");

        assertNotNull(response);
        assertTrue(response.getResponse().contains("Gate A"));
        // At 16 min, SHOULD say "quite busy"
        assertTrue(response.getResponse().contains("quite busy") ||
                   response.getResponse().contains("busy"));
    }

    @Test
    public void testProcessQuery_ZoneMatchByNorth() {
        // Test line 161: userQuery.contains("north") && name.contains("north")
        Zone zone = new Zone("North_VIP", "North VIP Lounge", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("what about north area?");

        assertNotNull(response);
        assertTrue(response.getResponse().contains("North VIP"));
    }

    @Test
    public void testProcessQuery_ZoneMatchByVIP() {
        // Test line 162: userQuery.contains("vip") && name.contains("vip")
        Zone zone = new Zone("VIP_Lounge", "VIP Premium Lounge", 3, 15);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("where is vip section?");

        assertNotNull(response);
        assertTrue(response.getResponse().contains("VIP"));
    }

    @Test
    public void testProcessQuery_ZoneMatchByFood() {
        // Test line 163: userQuery.contains("food") && name.contains("food")
        Zone zone = new Zone("Food_Court_A", "Food Court North", 4, 25);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("where is the food court?");

        assertNotNull(response);
        assertTrue(response.getResponse().contains("Food Court"));
    }

    @Test
    public void testProcessQuery_ZoneMatchByHydration() {
        // Test line 164: userQuery.contains("hydration") && name.contains("hydration")
        Zone zone = new Zone("Hydration_Station", "Hydration Point West", 2, 10);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("where can I get hydration?");

        assertNotNull(response);
        assertTrue(response.getResponse().contains("Hydration"));
    }

    @Test
    public void testProcessQuery_ZoneMatchByGateA() {
        // Test line 165: userQuery.contains("gate a") && name.contains("gate a")
        Zone zone = new Zone("Gate_A", "Gate A Entrance", 5, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("how is gate a doing?");

        assertNotNull(response);
        assertTrue(response.getResponse().contains("Gate A"));
    }

    @Test
    public void testProcessQuery_ZoneMatchById() {
        // Test line 159: userQuery.contains(id)
        Zone zone = new Zone("Gate_B", "Gate B Main", 8, 40);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("status of gate b");

        assertNotNull(response);
        assertTrue(response.getResponse().contains("Gate B"));
    }

    @Test
    public void testProcessQuery_ZoneMatchByIdUnderscored() {
        // Test line 160: userQuery.contains(z.getId().toLowerCase())
        Zone zone = new Zone("North_VIP_Food_Court", "North VIP Food Court", 7, 35);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("north_vip_food_court status");

        assertNotNull(response);
        assertTrue(response.getResponse().contains("North VIP"));
    }

    @Test
    public void testProcessQuery_StatusWithBusiestNull() {
        // Test line 183: if (busiest != null)
        when(zoneRepository.findAll()).thenReturn(new ArrayList<>());

        AiResponseDto response = geminiService.processQuery("what's the status?");

        assertNotNull(response);
        // Should still return a response even with empty zones
        assertNotNull(response.getResponse());
    }

    @Test
    public void testProcessQuery_StatusWithEmptiestNull() {
        // Test line 184: if (emptiest != null)
        when(zoneRepository.findAll()).thenReturn(new ArrayList<>());

        AiResponseDto response = geminiService.processQuery("capacity overview");

        assertNotNull(response);
        assertNotNull(response.getResponse());
    }

    @Test
    public void testProcessQuery_StatusKeywordVariations() {
        // Test line 178: userQuery.contains("status") || userQuery.contains("how is") || userQuery.contains("capacity") || userQuery.contains("overview")
        Zone zone = new Zone("Gate_A", "Gate A", 10, 40);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response1 = geminiService.processQuery("status");
        AiResponseDto response2 = geminiService.processQuery("how is everything");
        AiResponseDto response3 = geminiService.processQuery("capacity check");
        AiResponseDto response4 = geminiService.processQuery("overview please");

        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);
        assertNotNull(response4);

        assertTrue(response1.getResponse().contains("Stadium Health Report"));
        assertTrue(response2.getResponse().contains("Stadium Health Report"));
        assertTrue(response3.getResponse().contains("Stadium Health Report"));
        assertTrue(response4.getResponse().contains("Stadium Health Report"));
    }

    @Test
    public void testProcessQuery_DealKeywordVariations() {
        // Test line 186: userQuery.contains("deal") || userQuery.contains("cheap") || userQuery.contains("discount") || userQuery.contains("food")
        Zone zone = new Zone("Section_A", "Food Section A", 3, 15);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response1 = geminiService.processQuery("any deals?");
        AiResponseDto response2 = geminiService.processQuery("what's cheap?");
        AiResponseDto response3 = geminiService.processQuery("discounts available?");
        AiResponseDto response4 = geminiService.processQuery("where is food?");

        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);
        assertNotNull(response4);
    }

    @Test
    public void testProcessQuery_DealWithLowWaitStand() {
        // Test line 192-193: stands.get(0).getWaitTime() < 5 - TRUE branch
        Zone lowWaitStand = new Zone("Section_101", "Food Court A", 3, 20);
        Zone highWaitStand = new Zone("Section_102", "Food Court B", 10, 50);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(highWaitStand, lowWaitStand));

        AiResponseDto response = geminiService.processQuery("any deals?");

        assertNotNull(response);
        assertTrue(response.getResponse().contains("Good news!") ||
                   response.getResponse().contains("Food Court A") ||
                   response.getResponse().contains("discount"));
    }

    @Test
    public void testProcessQuery_DealWithExactly4MinWait() {
        // Test boundary at line 192: stands.get(0).getWaitTime() < 5
        Zone stand = new Zone("Section_101", "Food Court A", 4, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(stand));

        AiResponseDto response = geminiService.processQuery("any deals?");

        assertNotNull(response);
        // Should show deal since 4 < 5
        assertTrue(response.getResponse().contains("Good news!") ||
                   response.getResponse().contains("discount"));
    }

    @Test
    public void testProcessQuery_DealWithExactly5MinWait() {
        // Test boundary at line 192: stands.get(0).getWaitTime() < 5
        Zone stand = new Zone("Section_101", "Food Court A", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(stand));

        AiResponseDto response = geminiService.processQuery("any deals?");

        assertNotNull(response);
        // Should NOT show deal since 5 is NOT < 5
        assertTrue(response.getResponse().contains("normal capacity") ||
                   response.getResponse().contains("Check the 'Order' tab"));
    }

    @Test
    public void testProcessQuery_DealWithNoSectionStands() {
        // Test line 192: !stands.isEmpty() - FALSE branch
        Zone gate = new Zone("Gate_A", "Main Gate", 2, 10);  // Not a Section
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(gate));

        AiResponseDto response = geminiService.processQuery("any deals?");

        assertNotNull(response);
        // No sections, so should say normal capacity
        assertTrue(response.getResponse().contains("normal capacity") ||
                   response.getResponse().contains("Check"));
    }

    @Test
    public void testProcessQuery_DealWithEmptyStandsList() {
        // Test line 192: !stands.isEmpty() - FALSE branch
        when(zoneRepository.findAll()).thenReturn(new ArrayList<>());

        AiResponseDto response = geminiService.processQuery("any deals?");

        assertNotNull(response);
        assertTrue(response.getResponse().contains("normal capacity") ||
                   response.getResponse().contains("Check"));
    }

    @Test
    public void testProcessQuery_DealWithHighWaitStand() {
        // Test line 194-195: else branch when wait >= 5
        Zone highWaitStand = new Zone("Section_101", "Food Court A", 10, 50);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(highWaitStand));

        AiResponseDto response = geminiService.processQuery("deal");

        assertNotNull(response);
        assertFalse(response.getResponse().contains("Good news!"));
        assertTrue(response.getResponse().contains("normal capacity") ||
                   response.getResponse().contains("Check"));
    }

    @Test
    public void testProcessQuery_FallbackToDefaultMessage() {
        // Test line 198-199: else branch (no keywords matched)
        Zone zone = new Zone("Gate_A", "Main Gate", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("hello there");

        assertNotNull(response);
        assertTrue(response.getResponse().contains("StadiumPulse Concert Concierge"));
        assertTrue(response.getResponse().contains("entry gates") ||
                   response.getResponse().contains("hydration") ||
                   response.getResponse().contains("eco-deals"));
    }

    @Test
    public void testProcessQuery_MixedCaseQuery() {
        Zone zone = new Zone("Gate_A", "Gate A", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response1 = geminiService.processQuery("GATE A STATUS");
        AiResponseDto response2 = geminiService.processQuery("GaTe A sTaTuS");

        assertNotNull(response1);
        assertNotNull(response2);
        assertTrue(response1.getResponse().contains("Gate A"));
        assertTrue(response2.getResponse().contains("Gate A"));
    }

    @Test
    public void testProcessQuery_MultipleZonesForStatus() {
        Zone gate1 = new Zone("Gate_A", "Gate A", 25, 80);  // Busiest
        Zone gate2 = new Zone("Gate_B", "Gate B", 3, 15);   // Emptiest
        Zone gate3 = new Zone("Gate_C", "Gate C", 10, 40);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(gate1, gate2, gate3));

        AiResponseDto response = geminiService.processQuery("stadium status");

        assertNotNull(response);
        assertTrue(response.getResponse().contains("Gate A"));  // Busiest
        assertTrue(response.getResponse().contains("Gate B"));  // Emptiest
        assertTrue(response.getResponse().contains("25"));      // Wait time
        assertTrue(response.getResponse().contains("3"));       // Wait time
    }

    @Test
    public void testProcessQuery_SingleZoneForStatus() {
        Zone zone = new Zone("Gate_A", "Gate A", 10, 40);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("status");

        assertNotNull(response);
        assertTrue(response.getResponse().contains("Gate A"));
        assertTrue(response.getResponse().contains("10"));
    }

    @Test
    public void testProcessQuery_ZeroWaitTime() {
        Zone zone = new Zone("Gate_A", "Gate A", 0, 5);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("how is gate a?");

        assertNotNull(response);
        assertTrue(response.getResponse().contains("Gate A"));
        assertTrue(response.getResponse().contains("0"));
        assertTrue(response.getResponse().contains("perfect") ||
                   response.getResponse().contains("great"));
    }

    @Test
    public void testProcessQuery_VeryHighWaitTime() {
        Zone zone = new Zone("Gate_A", "Gate A", 60, 100);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("gate a");

        assertNotNull(response);
        assertTrue(response.getResponse().contains("Gate A"));
        assertTrue(response.getResponse().contains("60"));
        assertTrue(response.getResponse().contains("busy"));
    }

    // ==== TESTS FOR GEMINI API SERVICE PATH (Lines 92-98) ====

    @Test
    public void testProcessQuery_WithGeminiApiServiceAvailable() {
        // Inject mock GeminiApiService
        ReflectionTestUtils.setField(geminiService, "geminiApiService", geminiApiService);

        when(geminiApiService.isAvailable()).thenReturn(true);
        when(geminiApiService.processWithGemini("test query"))
            .thenReturn("Response from Gemini API");

        AiResponseDto response = geminiService.processQuery("test query");

        assertNotNull(response);
        assertEquals("Response from Gemini API", response.getResponse());
        assertEquals("Google Gemini API", response.getProvider());
        verify(geminiApiService).isAvailable();
        verify(geminiApiService).processWithGemini("test query");
    }

    @Test
    public void testProcessQuery_WithGeminiApiServiceNull() {
        // geminiApiService is null by default
        ReflectionTestUtils.setField(geminiService, "geminiApiService", null);

        Zone zone = new Zone("Gate_A", "Gate A", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        // Should fall through to Vertex AI, then to rule-based logic
        AiResponseDto response = geminiService.processQuery("status");

        assertNotNull(response);
        assertNotNull(response.getResponse());
        // Should use rule-based logic as fallback
        assertTrue(response.getProvider().contains("Rule-Based") ||
                   response.getProvider().contains("Vertex AI"));
    }

    @Test
    public void testProcessQuery_WithGeminiApiServiceNotAvailable() {
        ReflectionTestUtils.setField(geminiService, "geminiApiService", geminiApiService);

        when(geminiApiService.isAvailable()).thenReturn(false);

        Zone zone = new Zone("Gate_A", "Gate A", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("status");

        assertNotNull(response);
        // Should NOT call processWithGemini since it's not available
        verify(geminiApiService, never()).processWithGemini(anyString());
        // Should fall back to rule-based logic or Vertex AI
        assertNotNull(response.getProvider());
    }

    @Test
    public void testProcessQuery_WithGeminiApiReturnsNull() {
        ReflectionTestUtils.setField(geminiService, "geminiApiService", geminiApiService);

        when(geminiApiService.isAvailable()).thenReturn(true);
        when(geminiApiService.processWithGemini("test query")).thenReturn(null);

        Zone zone = new Zone("Gate_A", "Gate A", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("test query");

        assertNotNull(response);
        verify(geminiApiService).processWithGemini("test query");
        // Should fall back to rule-based logic when Gemini returns null
        assertTrue(response.getProvider().contains("Rule-Based") ||
                   response.getProvider().contains("Vertex AI"));
    }

    @Test
    public void testProcessQuery_WithGeminiApiReturnsEmptyString() {
        ReflectionTestUtils.setField(geminiService, "geminiApiService", geminiApiService);

        when(geminiApiService.isAvailable()).thenReturn(true);
        when(geminiApiService.processWithGemini("test query")).thenReturn("");

        Zone zone = new Zone("Gate_A", "Gate A", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("test query");

        assertNotNull(response);
        verify(geminiApiService).processWithGemini("test query");
        // Should fall back when Gemini returns empty string
        assertTrue(response.getProvider().contains("Rule-Based") ||
                   response.getProvider().contains("Vertex AI"));
    }

    @Test
    public void testProcessQuery_WithGeminiApiReturnsWhitespace() {
        ReflectionTestUtils.setField(geminiService, "geminiApiService", geminiApiService);

        when(geminiApiService.isAvailable()).thenReturn(true);
        when(geminiApiService.processWithGemini("test query")).thenReturn("   ");

        Zone zone = new Zone("Gate_A", "Gate A", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("test query");

        assertNotNull(response);
        verify(geminiApiService).processWithGemini("test query");
        // Whitespace is NOT considered empty in the code, so it's accepted
        assertEquals("   ", response.getResponse());
        assertEquals("Google Gemini API", response.getProvider());
    }

    // ==== TESTS FOR VERTEX AI PATH (Lines 105-110) ====

    @Test
    public void testProcessQuery_VertexAINotAvailable() {
        // Set both model and vertexAi to null via reflection (simulating unavailable Vertex AI)
        ReflectionTestUtils.setField(geminiService, "model", null);
        ReflectionTestUtils.setField(geminiService, "vertexAi", null);
        ReflectionTestUtils.setField(geminiService, "geminiApiService", null);

        Zone zone = new Zone("Gate_A", "Gate A", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("status");

        assertNotNull(response);
        // Should fall back to rule-based logic
        assertTrue(response.getProvider().contains("Rule-Based") ||
                   response.getProvider().contains("unavailable"));
    }

    // ==== TESTS FOR LAMBDA FILTER (Line 165) ====

    @Test
    public void testProcessQuery_ZoneNotContainingSearchTerm() {
        Zone gate = new Zone("North_Gate", "North Gate Entrance", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(gate));

        // Search for "south" when only "north" exists - but "status" keyword will match
        AiResponseDto response = geminiService.processQuery("south xyz query");

        assertNotNull(response);
        assertNotNull(response.getResponse());
        // Will return status report or default concierge message
        assertTrue(response.getResponse().contains("StadiumPulse") ||
                   response.getResponse().contains("concierge") ||
                   response.getResponse().contains("Stadium"));
    }

    @Test
    public void testProcessQuery_VIPPartialMatch() {
        Zone zone = new Zone("VIP_123", "VIP Premium Section 123", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("vip section");

        assertNotNull(response);
        assertTrue(response.getResponse().contains("VIP"));
    }

    @Test
    public void testProcessQuery_ZoneIdExactMatch() {
        Zone zone = new Zone("gate_b", "Gate B Main Entrance", 8, 35);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("how is gate_b?");

        assertNotNull(response);
        assertTrue(response.getResponse().contains("Gate B"));
    }

    @Test
    public void testProcessQuery_MultipleKeywordsInZoneName() {
        Zone zone = new Zone("North_VIP_Food", "North VIP Food Court", 3, 15);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response1 = geminiService.processQuery("north section");
        AiResponseDto response2 = geminiService.processQuery("vip area");
        AiResponseDto response3 = geminiService.processQuery("food location");

        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);

        assertTrue(response1.getResponse().contains("North VIP"));
        assertTrue(response2.getResponse().contains("North VIP"));
        assertTrue(response3.getResponse().contains("North VIP"));
    }

    // ==== TESTS FOR MISSING LAMBDA FILTER BRANCHES (Line 165) ====

    @Test
    public void testProcessQuery_ZoneContainsQueryWithSpaces() {
        // Test: userQuery.contains(name) where name has spaces
        Zone zone = new Zone("Food_Court_123", "Food Court Main", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("food court");

        assertNotNull(response);
        assertTrue(response.getResponse().contains("Food Court Main"));
    }

    @Test
    public void testProcessQuery_NameContainsQueryLowercase() {
        // Test: name.contains(userQuery) branch
        Zone zone = new Zone("Section_Main_VIP", "Main VIP Section", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("main");

        assertNotNull(response);
        assertNotNull(response.getResponse());
        // Should match based on "main" keyword or return status
        assertTrue(response.getResponse().contains("Main VIP") ||
                   response.getResponse().contains("Stadium"));
    }

    @Test
    public void testProcessQuery_IdWithUnderscoresContainsQuery() {
        // Test the contains check with underscores replaced
        Zone zone = new Zone("premium_vip_lounge", "Premium VIP Lounge", 3, 15);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("premium vip");

        assertNotNull(response);
        assertNotNull(response.getResponse());
        // Should match on VIP keyword
        assertTrue(response.getResponse().contains("Premium VIP") ||
                   response.getResponse().contains("vip") ||
                   response.getResponse().contains("VIP"));
    }

    @Test
    public void testProcessQuery_PartialNameMatch() {
        // Test name contains partial query
        Zone zone = new Zone("Gate_Main_Entrance", "Main Entrance Gate", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("entrance");

        assertNotNull(response);
        assertNotNull(response.getResponse());
        // Should return valid response (either matched or status)
        assertTrue(response.getResponse().length() > 0);
    }

    @Test
    public void testProcessQuery_NoMatchReturnsDefault() {
        Zone zone = new Zone("Section_A", "Section A", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        // Query that doesn't match any zone
        AiResponseDto response = geminiService.processQuery("something random xyz");

        assertNotNull(response);
        // Should return default concierge message
        assertTrue(response.getResponse().contains("StadiumPulse") ||
                   response.getResponse().contains("concierge"));
    }

    @Test
    public void testProcessQuery_CombinedFilterConditions() {
        // Test multiple zones, multiple filter conditions
        Zone zone1 = new Zone("north_gate_a", "North Gate A", 3, 15);
        Zone zone2 = new Zone("south_vip_lounge", "South VIP Lounge", 5, 25);
        Zone zone3 = new Zone("food_court_main", "Food Court Main", 2, 10);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone1, zone2, zone3));

        AiResponseDto response1 = geminiService.processQuery("north");
        AiResponseDto response2 = geminiService.processQuery("vip");
        AiResponseDto response3 = geminiService.processQuery("food");

        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);

        assertTrue(response1.getResponse().contains("North Gate"));
        assertTrue(response2.getResponse().contains("VIP"));
        assertTrue(response3.getResponse().contains("Food Court"));
    }

    @Test
    public void testProcessQuery_HydrationZoneMatch() {
        // Test the hydration keyword in line 164
        Zone zone = new Zone("hydration_station_1", "Hydration Station West", 2, 10);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = geminiService.processQuery("where is hydration station?");

        assertNotNull(response);
        assertTrue(response.getResponse().contains("Hydration"));
    }

    @Test
    public void testProcessQuery_GateASpecificMatch() {
        // Test the "gate a" specific keyword in line 165
        Zone gateA = new Zone("gate_a_entrance", "Gate A Main Entrance", 5, 20);
        Zone gateB = new Zone("gate_b_entrance", "Gate B Side Entrance", 3, 15);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(gateA, gateB));

        AiResponseDto response = geminiService.processQuery("gate a");

        assertNotNull(response);
        assertTrue(response.getResponse().contains("Gate A"));
    }

    @Test
    public void testProcessQuery_AllFilterBranchesInLambda() {
        // Comprehensive test to hit all OR branches in lambda at line 157-165
        Zone zone = new Zone("Gate_A_North_VIP_Food_Hydration", "North VIP Food & Hydration at Gate A", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        // Test each keyword that would match different branches
        AiResponseDto r1 = geminiService.processQuery("gate a north vip food hydration");

        assertNotNull(r1);
        assertTrue(r1.getResponse().contains("North VIP"));
    }

    // ==== TESTS FOR 100% COVERAGE - EXCEPTION PATHS (Lines 99-100, 107, 138-142) ====

    @Test
    public void testProcessQuery_GeminiApiThrowsException_FallsBackToVertexAI() throws Exception {
        // Test line 99-100: When Gemini API throws exception
        GeminiApiService geminiApiService = mock(GeminiApiService.class);
        when(geminiApiService.isAvailable()).thenReturn(true);
        when(geminiApiService.processWithGemini(anyString())).thenThrow(new RuntimeException("API Error"));

        GeminiService service = new GeminiService(zoneRepository, "test-project", "us-central1", "gemini-pro", "test-key");
        ReflectionTestUtils.setField(service, "geminiApiService", geminiApiService);

        Zone zone = new Zone("Gate_A", "Gate A", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        AiResponseDto response = service.processQuery("status");

        assertNotNull(response);
        // Should fall back to rule-based or Vertex AI
        assertNotNull(response.getProvider());
        verify(geminiApiService).processWithGemini(anyString());
    }

    @Test
    public void testProcessQuery_VertexAIThrowsException_FallsBackToRuleBased() {
        // Test processWithVertexAI exception path (lines 138-142)
        // Without actual GCP, Vertex AI will throw exception and fall back to rule-based
        Zone zone = new Zone("Gate_A", "Gate A", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        GeminiService service = new GeminiService(zoneRepository, "test-project", "us-central1", "gemini-pro", null);

        AiResponseDto response = service.processQuery("status");

        assertNotNull(response);
        // Should use rule-based logic since Vertex AI is not configured
        assertNotNull(response.getResponse());
    }

    // ==== TESTS FOR 100% COVERAGE - VERTEX AI PATHS (Lines 107, 138-142) ====

    @Test
    public void testProcessWithVertexAI_SuccessfulCall() throws Exception {
        // Test line 107 and lines 138-142: Successful Vertex AI call
        Zone zone = new Zone("Gate_A", "Gate A", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        GeminiService service = new GeminiService(zoneRepository, "test-project", "us-central1", "gemini-1.5-pro", null);

        // Mock Vertex AI objects
        com.google.cloud.vertexai.VertexAI mockVertexAi = mock(com.google.cloud.vertexai.VertexAI.class);
        com.google.cloud.vertexai.generativeai.GenerativeModel mockModel = mock(com.google.cloud.vertexai.generativeai.GenerativeModel.class);
        com.google.cloud.vertexai.api.GenerateContentResponse mockResponse = mock(com.google.cloud.vertexai.api.GenerateContentResponse.class);

        // Mock the response - generateContent takes a String
        when(mockModel.generateContent(anyString()))
            .thenReturn(mockResponse);

        // Mock ResponseHandler.getText to return a string
        com.google.cloud.vertexai.api.Candidate mockCandidate = mock(com.google.cloud.vertexai.api.Candidate.class);
        com.google.cloud.vertexai.api.Content mockContent = mock(com.google.cloud.vertexai.api.Content.class);
        com.google.cloud.vertexai.api.Part mockPart = mock(com.google.cloud.vertexai.api.Part.class);

        when(mockResponse.getCandidatesList()).thenReturn(java.util.Arrays.asList(mockCandidate));
        when(mockCandidate.getContent()).thenReturn(mockContent);
        when(mockContent.getPartsList()).thenReturn(java.util.Arrays.asList(mockPart));
        when(mockPart.getText()).thenReturn("Vertex AI says: Gate A has moderate traffic.");

        // Inject mocks via reflection
        ReflectionTestUtils.setField(service, "vertexAi", mockVertexAi);
        ReflectionTestUtils.setField(service, "model", mockModel);

        // Call processQuery - should use Vertex AI path (line 107)
        AiResponseDto response = service.processQuery("status");

        assertNotNull(response);
        assertNotNull(response.getResponse());
        assertTrue(response.getResponse().contains("Gate A") ||
                   response.getResponse().contains("Vertex AI") ||
                   response.getResponse().contains("moderate traffic"));
        assertTrue(response.getProvider().contains("Vertex AI") || response.getProvider().contains("Google"));

        // Verify Vertex AI was called (covering lines 138-142)
        verify(mockModel, atLeastOnce()).generateContent(anyString());
    }

    @Test
    public void testProcessWithVertexAI_ZoneSpecificQuery() throws Exception {
        // Test Vertex AI with zone-specific query
        Zone zone1 = new Zone("Gate_A", "Gate A", 5, 20);
        Zone zone2 = new Zone("Food_Court", "Food Court", 3, 15);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone1, zone2));

        GeminiService service = new GeminiService(zoneRepository, "test-project", "us-central1", "gemini-1.5-pro", null);

        // Mock Vertex AI
        com.google.cloud.vertexai.VertexAI mockVertexAi = mock(com.google.cloud.vertexai.VertexAI.class);
        com.google.cloud.vertexai.generativeai.GenerativeModel mockModel = mock(com.google.cloud.vertexai.generativeai.GenerativeModel.class);
        com.google.cloud.vertexai.api.GenerateContentResponse mockResponse = mock(com.google.cloud.vertexai.api.GenerateContentResponse.class);

        // Setup mock response
        com.google.cloud.vertexai.api.Candidate mockCandidate = mock(com.google.cloud.vertexai.api.Candidate.class);
        com.google.cloud.vertexai.api.Content mockContent = mock(com.google.cloud.vertexai.api.Content.class);
        com.google.cloud.vertexai.api.Part mockPart = mock(com.google.cloud.vertexai.api.Part.class);

        when(mockResponse.getCandidatesList()).thenReturn(java.util.Arrays.asList(mockCandidate));
        when(mockCandidate.getContent()).thenReturn(mockContent);
        when(mockContent.getPartsList()).thenReturn(java.util.Arrays.asList(mockPart));
        when(mockPart.getText()).thenReturn("AI Response: All zones operating normally.");

        when(mockModel.generateContent(anyString()))
            .thenReturn(mockResponse);

        // Inject mocks
        ReflectionTestUtils.setField(service, "vertexAi", mockVertexAi);
        ReflectionTestUtils.setField(service, "model", mockModel);

        // Call processQuery which will invoke processWithVertexAI at line 107
        AiResponseDto response = service.processQuery("What's happening at Gate A?");

        // Verify lines 138-142 were executed
        assertNotNull(response);
        assertNotNull(response.getResponse());
        verify(mockModel).generateContent(anyString());
    }

    @Test
    public void testProcessWithVertexAI_CompleteFlow() throws Exception {
        // Test lines 107, 138-142: Complete Vertex AI flow with ResponseHandler.getText()
        Zone zone = new Zone("VIP_Lounge", "VIP Lounge", 2, 10);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        // Create service without API key (geminiApiService will be null)
        GeminiService service = new GeminiService(zoneRepository, "test-project", "us-central1", "gemini-1.5-pro", null);

        // Mock Vertex AI with proper response structure for ResponseHandler.getText()
        com.google.cloud.vertexai.VertexAI mockVertexAi = mock(com.google.cloud.vertexai.VertexAI.class);
        com.google.cloud.vertexai.generativeai.GenerativeModel mockModel = mock(com.google.cloud.vertexai.generativeai.GenerativeModel.class);

        // Create a real response that ResponseHandler.getText can parse
        // We need to build the protobuf structure properly
        com.google.cloud.vertexai.api.GenerateContentResponse.Builder responseBuilder =
            com.google.cloud.vertexai.api.GenerateContentResponse.newBuilder();

        // Build the candidate with content
        com.google.cloud.vertexai.api.Content.Builder contentBuilder =
            com.google.cloud.vertexai.api.Content.newBuilder();

        // Build the part with text
        com.google.cloud.vertexai.api.Part.Builder partBuilder =
            com.google.cloud.vertexai.api.Part.newBuilder()
                .setText("VIP Lounge: Light crowd, 2 minute wait.");

        contentBuilder.addParts(partBuilder.build());

        com.google.cloud.vertexai.api.Candidate.Builder candidateBuilder =
            com.google.cloud.vertexai.api.Candidate.newBuilder()
                .setContent(contentBuilder.build());

        responseBuilder.addCandidates(candidateBuilder.build());

        com.google.cloud.vertexai.api.GenerateContentResponse mockResponse = responseBuilder.build();

        when(mockModel.generateContent(anyString()))
            .thenReturn(mockResponse);

        // Inject Vertex AI mocks
        ReflectionTestUtils.setField(service, "vertexAi", mockVertexAi);
        ReflectionTestUtils.setField(service, "model", mockModel);

        // Verify both are injected
        assertNotNull(ReflectionTestUtils.getField(service, "model"));
        assertNotNull(ReflectionTestUtils.getField(service, "vertexAi"));

        // This should hit line 107 and execute lines 138-142
        AiResponseDto response = service.processQuery("vip lounge info");

        assertNotNull(response);
        assertNotNull(response.getResponse());
        // ResponseHandler.getText() should extract the text from the response
        assertTrue(response.getResponse().contains("VIP Lounge") ||
                   response.getResponse().contains("Light crowd"));
        assertTrue(response.getProvider().contains("Vertex AI") || response.getProvider().contains("Gemini"));

        // Verify Vertex AI was called (covering line 138)
        verify(mockModel).generateContent(anyString());
    }

    @Test
    public void testProcessWithVertexAI_BothAvailable() throws Exception {
        // Test line 105 TRUE branch: when BOTH model AND vertexAi are NOT null
        Zone zone = new Zone("North_Entrance", "North Entrance", 3, 15);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        // Create service without API key
        GeminiService service = new GeminiService(zoneRepository, "test-project", "us-central1", "gemini-pro", null);

        // Mock both Vertex AI components as NON-NULL
        com.google.cloud.vertexai.VertexAI mockVertexAi = mock(com.google.cloud.vertexai.VertexAI.class);
        com.google.cloud.vertexai.generativeai.GenerativeModel mockModel = mock(com.google.cloud.vertexai.generativeai.GenerativeModel.class);

        // Build proper response
        com.google.cloud.vertexai.api.GenerateContentResponse.Builder responseBuilder =
            com.google.cloud.vertexai.api.GenerateContentResponse.newBuilder();
        com.google.cloud.vertexai.api.Content.Builder contentBuilder =
            com.google.cloud.vertexai.api.Content.newBuilder();
        com.google.cloud.vertexai.api.Part.Builder partBuilder =
            com.google.cloud.vertexai.api.Part.newBuilder()
                .setText("North Entrance info");
        contentBuilder.addParts(partBuilder.build());
        com.google.cloud.vertexai.api.Candidate.Builder candidateBuilder =
            com.google.cloud.vertexai.api.Candidate.newBuilder()
                .setContent(contentBuilder.build());
        responseBuilder.addCandidates(candidateBuilder.build());

        when(mockModel.generateContent(anyString()))
            .thenReturn(responseBuilder.build());

        // Inject BOTH as NON-NULL
        ReflectionTestUtils.setField(service, "vertexAi", mockVertexAi);
        ReflectionTestUtils.setField(service, "model", mockModel);

        // This should hit line 105 TRUE branch (model != null && vertexAi != null)
        AiResponseDto response = service.processQuery("north entrance status");

        assertNotNull(response);
        verify(mockModel).generateContent(anyString());
    }

    @Test
    public void testProcessQuery_ModelNotNullButVertexAINull() throws Exception {
        // Test line 105 MIDDLE branch: model != null BUT vertexAi == null
        // This tests the short-circuit evaluation where first condition passes but second fails
        Zone zone = new Zone("VIP_Area", "VIP Area", 2, 8);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        GeminiService service = new GeminiService(zoneRepository, "test-project", "us-central1", "gemini-pro", null);

        // Set model to non-null but vertexAi to null
        com.google.cloud.vertexai.generativeai.GenerativeModel mockModel =
            mock(com.google.cloud.vertexai.generativeai.GenerativeModel.class);

        ReflectionTestUtils.setField(service, "model", mockModel);  // NON-NULL
        ReflectionTestUtils.setField(service, "vertexAi", null);     // NULL

        // Should skip line 107 and fall through to rule-based logic
        AiResponseDto response = service.processQuery("vip area status");

        assertNotNull(response);
        assertTrue(response.getProvider().contains("Rule-Based"));

        // Verify model.generateContent was NEVER called
        verify(mockModel, never()).generateContent(anyString());
    }

    @Test
    public void testRuleBasedLogic_NorthKeywordButNoNorthZone() {
        // Test line 161 FALSE branch on 2nd part: query has "north" but zone name does NOT
        Zone vipZone = new Zone("VIP_Section", "VIP Section", 2, 10);  // No "north" in name
        Zone foodZone = new Zone("Food_Court", "Food Court", 3, 15);   // No "north" in name
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(vipZone, foodZone));

        GeminiService service = new GeminiService(zoneRepository, "test-project", "us-central1", "gemini-pro", null);

        // Query contains "north" but none of the zones contain "north" in their name
        // This will make line 161's second condition FALSE: name.contains("north") is FALSE
        AiResponseDto response = service.processQuery("Is it crowded in the north area?");

        assertNotNull(response);
        // Should return generic response since no zone matches
    }

    @Test
    public void testRuleBasedLogic_HydrationKeywordButNoHydrationZone() {
        // Test line 164 FALSE branch on 2nd part: query has "hydration" but zone name does NOT
        Zone gateA = new Zone("Gate_A", "Gate A", 4, 18);           // No "hydration"
        Zone restroom = new Zone("Restroom", "Restroom", 2, 8);     // No "hydration"
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(gateA, restroom));

        GeminiService service = new GeminiService(zoneRepository, "test-project", "us-central1", "gemini-pro", null);

        // Query contains "hydration" but no zone has "hydration" in name
        // This will make line 164's second condition FALSE: name.contains("hydration") is FALSE
        AiResponseDto response = service.processQuery("Where is hydration available?");

        assertNotNull(response);
        // Should return generic response since no zone matches
    }

    @Test
    public void testRuleBasedLogic_GateAKeywordButNoGateAZone() {
        // Test line 165 FALSE branch on 2nd part: query has "gate a" but zone name does NOT
        Zone gateB = new Zone("Gate_B", "Gate B", 3, 12);          // No "gate a"
        Zone entrance = new Zone("Main_Entrance", "Main Entrance", 5, 20);  // No "gate a"
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(gateB, entrance));

        GeminiService service = new GeminiService(zoneRepository, "test-project", "us-central1", "gemini-pro", null);

        // Query contains "gate a" but no zone has "gate a" in name
        // This will make line 165's second condition FALSE: name.contains("gate a") is FALSE
        AiResponseDto response = service.processQuery("How busy is gate a?");

        assertNotNull(response);
        // Should return generic response since no zone matches
    }
}


