package com.byteme.app;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class GamificationControllerTest {

    private MockMvc mockMvc;

    @Mock private OrganisationRepository orgRepo;
    @Mock private OrgOrderRepository orderRepo;
    @Mock private BadgeRepository badgeRepo;
    @Mock private OrganisationBadgeRepository orgBadgeRepo;

    @InjectMocks
    private GamificationController gamificationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 1. Create and configure the ObjectMapper strictly for the test environment
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // This line prevents the [2023, 10, 1] array format
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // 2. Build MockMvc using the configured mapper
        mockMvc = MockMvcBuilders.standaloneSetup(gamificationController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void testGetStreak_Success() throws Exception {
        UUID orgId = UUID.randomUUID();
        Organisation org = new Organisation();
        org.setCurrentStreakWeeks(5);
        org.setBestStreakWeeks(10);
        org.setLastOrderWeekStart(LocalDate.of(2023, 10, 1));

        when(orgRepo.findById(orgId)).thenReturn(Optional.of(org));

        // Now this will correctly match the String "2023-10-01"
        mockMvc.perform(get("/api/gamification/streak/{orgId}", orgId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStreakWeeks").value(5))
                .andExpect(jsonPath("$.lastOrderWeekStart").value("2023-10-01"));
    }

    @Test
    void testGetStats_Success() throws Exception {
        UUID orgId = UUID.randomUUID();
        Organisation org = new Organisation();
        org.setTotalOrders(50);
        org.setCurrentStreakWeeks(3);
        org.setBestStreakWeeks(8);

        when(orgRepo.findById(orgId)).thenReturn(Optional.of(org));
        when(orgBadgeRepo.findByOrgId(orgId)).thenReturn(Collections.singletonList(new OrganisationBadge()));

        mockMvc.perform(get("/api/gamification/stats/{orgId}", orgId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(50))
                .andExpect(jsonPath("$.badgesEarned").value(1));
    }

    @Test
    void testGetStreak_NotFound() throws Exception {
        UUID orgId = UUID.randomUUID();
        when(orgRepo.findById(orgId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/gamification/streak/{orgId}", orgId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllBadges() throws Exception {
        when(badgeRepo.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/gamification/badges"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void testDTOAccessors() {
        LocalDate date = LocalDate.now();
        GamificationController.StreakResponse streak = new GamificationController.StreakResponse(1, 2, date);
        assertEquals(1, streak.getCurrentStreakWeeks());
        
        GamificationController.StatsResponse stats = new GamificationController.StatsResponse(10, 5, 10, 3);
        assertEquals(10, stats.getTotalOrders());
    }
}