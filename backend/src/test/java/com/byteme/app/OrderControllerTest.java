package com.byteme.app;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class OrderControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private OrgOrderRepository orderRepo;
    @Mock private BundlePostingRepository bundleRepo;
    @Mock private OrganisationRepository orgRepo;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        ObjectMapper testMapper = new ObjectMapper();
        testMapper.registerModule(new JavaTimeModule());
        testMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(testMapper))
                .build();
    }

    @Test
    void testCreateOrder_InsufficientStock() throws Exception {
        UUID bundleId = UUID.randomUUID();
        
        BundlePosting bundle = new BundlePosting();
        bundle.setQuantityTotal(5);
        bundle.setQuantityReserved(5); // Fully booked
        bundle.setStatus(BundlePosting.Status.ACTIVE);

        OrderController.CreateOrderRequest req = new OrderController.CreateOrderRequest();
        req.setPostingId(bundleId);
        req.setQuantity(1); 

        when(bundleRepo.findById(bundleId)).thenReturn(Optional.of(bundle));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                // FIXED: containsString handles both "Text" and "\"Text\"" formats
                .andExpect(content().string(containsString("Not enough bundles available")));
    }

    @Test
    void testCreateOrder_Success() throws Exception {
        UUID bundleId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        
        BundlePosting bundle = new BundlePosting();
        bundle.setPriceCents(1000);
        bundle.setDiscountPct(0);
        bundle.setQuantityTotal(10);
        bundle.setQuantityReserved(0);
        bundle.setStatus(BundlePosting.Status.ACTIVE);
        
        Seller seller = new Seller();
        seller.setName("Test Shop");
        seller.setLocationText("Test Lane");
        bundle.setSeller(seller);

        when(bundleRepo.findById(bundleId)).thenReturn(Optional.of(bundle));
        when(orgRepo.findById(any())).thenReturn(Optional.of(new Organisation()));
        when(orderRepo.save(any())).thenAnswer(i -> {
            OrgOrder o = i.getArgument(0);
            o.setOrderId(UUID.randomUUID());
            return o;
        });

        OrderController.CreateOrderRequest req = new OrderController.CreateOrderRequest();
        req.setPostingId(bundleId);
        req.setOrgId(orgId);
        req.setQuantity(1);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(1));
        
        assertEquals(1, bundle.getQuantityReserved());
    }

    @Test
    void testCollect_Success() throws Exception {
        UUID orderId = UUID.randomUUID();
        Organisation org = new Organisation();
        org.setLastOrderWeekStart(LocalDate.now().minusWeeks(1));

        OrgOrder order = new OrgOrder();
        order.setStatus(OrgOrder.Status.RESERVED);
        order.setOrganisation(org);

        when(orderRepo.findById(orderId)).thenReturn(Optional.of(order));

        mockMvc.perform(post("/api/orders/{id}/collect", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertEquals(OrgOrder.Status.COLLECTED, order.getStatus());
        verify(orgRepo).save(org);
    }

    @Test
    void testCancel_Success() throws Exception {
        UUID orderId = UUID.randomUUID();
        BundlePosting bundle = new BundlePosting();
        bundle.setQuantityReserved(5);

        OrgOrder order = new OrgOrder();
        order.setStatus(OrgOrder.Status.RESERVED);
        order.setQuantity(2);
        order.setPosting(bundle);

        when(orderRepo.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(post("/api/orders/{id}/cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertEquals(3, bundle.getQuantityReserved());
    }

    @Test
    void testGetBySeller() throws Exception {
        UUID sellerId = UUID.randomUUID();
        when(orderRepo.findByPostingSellerSellerId(sellerId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/orders/seller/{sellerId}", sellerId))
                .andExpect(status().isOk());
    }
}