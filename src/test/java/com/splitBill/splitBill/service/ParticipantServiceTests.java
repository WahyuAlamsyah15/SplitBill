package com.splitBill.splitBill.service;

import com.splitBill.splitBill.dto.request.AddParticipantRequest;
import com.splitBill.splitBill.dto.request.AssignItemRequest;
import com.splitBill.splitBill.dto.request.CreateBillRequest;
import com.splitBill.splitBill.handler.ResourceNotFoundException;
import com.splitBill.splitBill.model.*;
import com.splitBill.splitBill.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ParticipantServiceTests {

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private RestoRepository restoRepository;

    @Autowired
    private BillItemRepository billItemRepository;

    @Autowired
    private BillParticipantRepository billParticipantRepository;

    @Autowired
    private ItemAssignmentRepository itemAssignmentRepository;

    private Resto testResto;
    private User testUser;
    private static final String TENANT_ID = "testTenant";

    @BeforeEach
    void setUp() {
        // Clear all data before each test
        itemAssignmentRepository.deleteAll();
        billItemRepository.deleteAll();
        billParticipantRepository.deleteAll();
        billRepository.deleteAll();
        restoRepository.deleteAll();

        // Setup test user for security context
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setTenantDbName(TENANT_ID);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())
        );

        // Setup a test resto
        testResto = new Resto();
        testResto.setName("Test Resto");
        testResto.setTenantId(TENANT_ID);
        testResto = restoRepository.save(testResto);
    }

    private Bill createTestBill() {
        CreateBillRequest createBillRequest = new CreateBillRequest();
        createBillRequest.setRestoId(testResto.getId().toString());
        createBillRequest.setNote("Test Bill");
        Bill bill = new Bill();
        bill.setResto(testResto);
        bill.setNote(createBillRequest.getNote());
        bill.setTenantId(TENANT_ID);
        return billRepository.save(bill);
    }

    private BillParticipant addTestParticipant(Bill bill, String name) {
        AddParticipantRequest request = new AddParticipantRequest();
        request.setName(name);
        BillParticipant participant = new BillParticipant();
        participant.setBill(bill);
        participant.setName(request.getName());
        participant.setTenantId(TENANT_ID);
        BillParticipant savedParticipant = billParticipantRepository.save(participant);
        bill.getParticipants().add(savedParticipant); // Explicitly add to the bill's collection
        // Ensure the bill entity in the current persistence context is updated
        billRepository.save(bill);
        return savedParticipant;
    }

    private BillItem addTestItem(Bill bill, String name, BigDecimal price, int quantity) {
        BillItem item = new BillItem();
        item.setBill(bill);
        item.setName(name);
        item.setPrice(price);
        item.setQuantity(quantity);
        item.setTenantId(TENANT_ID);
        return billItemRepository.save(item);
    }

    private void assignItemToParticipant(Bill bill, BillItem item, BillParticipant participant, int quantityTaken) {
        ItemAssignment assignment = new ItemAssignment();
        assignment.setBillItem(item);
        assignment.setParticipant(participant);
        assignment.setQuantityTaken(quantityTaken);
        assignment.setTenantId(TENANT_ID);
        itemAssignmentRepository.save(assignment);
    }

    @Test
    @Transactional
    void testDeleteParticipant_NoAssignedItems() {
        Bill bill = createTestBill();
        BillParticipant p1 = addTestParticipant(bill, "Alice");

        assertNotNull(billParticipantRepository.findById(p1.getId()).orElse(null));
        
        participantService.delete(bill.getId().toString(), p1.getId().toString());

        assertTrue(billParticipantRepository.findById(p1.getId()).isEmpty());
    }

    @Test
    @Transactional
    void testDeleteParticipant_WithAssignedItems() {
        Bill bill = createTestBill();
        BillItem item1 = addTestItem(bill, "Pizza", new BigDecimal("100.00"), 1); // Quantity: 1
        BillParticipant p1 = addTestParticipant(bill, "Bob");
        
        assignItemToParticipant(bill, item1, p1, 1); // Bob takes 1
        
        assertEquals(1, itemAssignmentRepository.findAll().size());
        assertNotNull(billParticipantRepository.findById(p1.getId()).orElse(null));

        participantService.delete(bill.getId().toString(), p1.getId().toString());

        assertTrue(billParticipantRepository.findById(p1.getId()).isEmpty());
        assertTrue(itemAssignmentRepository.findAll().isEmpty()); // Verify assignments are deleted
    }

    @Test
    @Transactional
    void testDeleteParticipant_NonExistentParticipant() {
        Bill bill = createTestBill();
        UUID nonExistentId = UUID.randomUUID();

        assertThrows(ResourceNotFoundException.class, () -> participantService.delete(bill.getId().toString(), nonExistentId.toString()));
    }

    @Test
    @Transactional
    void testDeleteParticipant_ParticipantNotInBill() {
        Bill bill1 = createTestBill();
        BillParticipant p1 = addTestParticipant(bill1, "Charlie");

        Bill bill2 = createTestBill(); // Another bill
        
        // Try to delete p1 from bill2
        assertThrows(ResourceNotFoundException.class, () -> participantService.delete(bill2.getId().toString(), p1.getId().toString()));
    }
}
