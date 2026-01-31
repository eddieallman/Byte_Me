package com.byteme.app;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private UserAccountRepository userRepo;
    @Mock private SellerRepository sellerRepo;
    @Mock private OrganisationRepository orgRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testDTOAccessors() {
        // Testing the inner class RegisterRequest
        AuthController.RegisterRequest req = new AuthController.RegisterRequest();
        req.setLocation("New York");
        assertEquals("New York", req.getLocation());
        
        // Fix: Use AuthController.AuthResponse
        AuthController.AuthResponse res = new AuthController.AuthResponse(
            "tok", UUID.randomUUID(), UUID.randomUUID(), "e@e.com", UserAccount.Role.SELLER
        );
        assertEquals("tok", res.getToken());
    }

    @Test
    void testLogin_Success() throws Exception {
        AuthController.LoginRequest req = new AuthController.LoginRequest();
        req.setEmail("test@byteme.com");
        req.setPassword("password123");

        UserAccount user = new UserAccount();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@byteme.com");
        user.setPasswordHash("hashed_string");
        user.setRole(UserAccount.Role.SELLER);

        when(userRepo.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"))
                .andExpect(jsonPath("$.email").value("test@byteme.com"));
    }

    @Test
    void testMe_ReturnsProfile() throws Exception {
        UUID mockUserId = UUID.randomUUID();
        UserAccount user = new UserAccount();
        user.setUserId(mockUserId);
        user.setRole(UserAccount.Role.SELLER);

        // Mock SecurityContextHolder behavior
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockUserId);
        when(userRepo.findById(mockUserId)).thenReturn(Optional.of(user));
        
        // Mock Seller lookup
        Seller seller = new Seller();
        seller.setSellerId(UUID.randomUUID());
        when(sellerRepo.findByUserUserId(mockUserId)).thenReturn(Optional.of(seller));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileId").value(seller.getSellerId().toString()));
    }
}