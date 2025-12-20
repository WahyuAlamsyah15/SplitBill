package com.splitBill.splitBill.service;

import com.splitBill.splitBill.dto.request.AddParticipantRequest;
import com.splitBill.splitBill.dto.request.AssignItemRequest;
import com.splitBill.splitBill.dto.request.CreateBillRequest;
import com.splitBill.splitBill.dto.request.UpdateTaxServiceRequest;
import com.splitBill.splitBill.dto.response.SplitResultResponse;
import com.splitBill.splitBill.handler.BadRequestException;
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
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class BillServiceTests {

    @Autowired
    private BillService billService;

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
        // Create the bill through the service, which will save it to the repository
        billService.createBill(createBillRequest);
        // Fetch the managed Bill object to ensure collections are correctly initialized if needed,
        // though with eager fetching in calculateSplit, this might be less critical here.
        return billRepository.findById(UUID.fromString(billService.getAllBills().get(0).get("id").toString())).orElseThrow();
    }

    private BillItem addTestItem(Bill bill, String name, BigDecimal price, int quantity) {
        BillItem item = new BillItem();
        item.setBill(bill);
        item.setName(name);
        item.setPrice(price);
        item.setQuantity(quantity);
        item.setTenantId(TENANT_ID);
        BillItem savedItem = billItemRepository.save(item);
        bill.getItems().add(savedItem); // Explicitly add to the bill's collection
        // Ensure the bill entity in the current persistence context is updated
        billRepository.save(bill); 
        return savedItem;
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

    private void assignItemToParticipant(Bill bill, BillItem item, BillParticipant participant, int quantityTaken) {
        AssignItemRequest request = new AssignItemRequest();
        request.setQuantityTaken(quantityTaken);
        billService.assignItem(bill.getId().toString(), item.getId().toString(), participant.getId().toString(), request);
    }

    @Test
    @Transactional
    void testCalculateSplit_Basic() {
        Bill bill = createTestBill();
        BillItem item1 = addTestItem(bill, "Pizza", new BigDecimal("100.00"), 2); // Ubah dari 1 menjadi 2
        BillParticipant p1 = addTestParticipant(bill, "Alice");
        BillParticipant p2 = addTestParticipant(bill, "Bob");

        assignItemToParticipant(bill, item1, p1, 1);
        assignItemToParticipant(bill, item1, p2, 1);

        SplitResultResponse result = billService.calculateSplit(bill.getId().toString());

        assertNotNull(result);
        assertEquals(new BigDecimal("200.00").setScale(2, RoundingMode.HALF_UP), result.getGrandTotal());
        assertEquals(2, result.getResults().size());

        Map<String, Object> aliceResult = result.getResults().stream()
                .filter(r -> "Alice".equals(r.get("participant")))
                .findFirst().orElseThrow();
        assertEquals(new BigDecimal("100.00").setScale(2, RoundingMode.HALF_UP), aliceResult.get("amountToPay"));

        Map<String, Object> bobResult = result.getResults().stream()
                .filter(r -> "Bob".equals(r.get("participant")))
                .findFirst().orElseThrow();
        assertEquals(new BigDecimal("100.00").setScale(2, RoundingMode.HALF_UP), bobResult.get("amountToPay"));
    }

    @Test
    @Transactional
    void testCalculateSplit_WithPercentageTaxAndService() {
        Bill bill = createTestBill();
        BillItem item1 = addTestItem(bill, "Burger", new BigDecimal("50.00"), 2); // Total 100
        BillParticipant p1 = addTestParticipant(bill, "Charlie");
        BillParticipant p2 = addTestParticipant(bill, "David");

        assignItemToParticipant(bill, item1, p1, 1);
        assignItemToParticipant(bill, item1, p2, 1);

        UpdateTaxServiceRequest taxServiceRequest = new UpdateTaxServiceRequest();
        taxServiceRequest.setTaxType(FeeType.PERCENT);
        taxServiceRequest.setTaxValue(new BigDecimal("10.00")); // 10% tax
        taxServiceRequest.setServiceType(FeeType.PERCENT);
        taxServiceRequest.setServiceValue(new BigDecimal("5.00")); // 5% service
        billService.updateTaxService(bill.getId().toString(), taxServiceRequest);

        SplitResultResponse result = billService.calculateSplit(bill.getId().toString());

        assertNotNull(result);
        // totalItemsDistributed = 100.00
        // tax = 10% of 100 = 10.00
        // service = 5% of 100 = 5.00
        // Grand Total = 100 + 10 + 5 = 115.00
        assertEquals(new BigDecimal("115.00").setScale(2, RoundingMode.HALF_UP), result.getGrandTotal());
        assertEquals(2, result.getResults().size());

        // Each person gets 50.00 subtotal
        // proportional tax for each = (50/100) * 10 = 5.00
        // proportional service for each = (50/100) * 5 = 2.50
        // Each person total = 50 + 5 + 2.50 = 57.50
        // Due to rounding distribution to first person ("Charlie")
        Map<String, Object> charlieResult = result.getResults().stream()
                .filter(r -> "Charlie".equals(r.get("participant")))
                .findFirst().orElseThrow();
        assertEquals(new BigDecimal("57.50").setScale(2, RoundingMode.HALF_UP), charlieResult.get("amountToPay"));

        Map<String, Object> davidResult = result.getResults().stream()
                .filter(r -> "David".equals(r.get("participant")))
                .findFirst().orElseThrow();
        assertEquals(new BigDecimal("57.50").setScale(2, RoundingMode.HALF_UP), davidResult.get("amountToPay"));
    }

    @Test
    @Transactional
    void testCalculateSplit_WithAmountTaxAndService() {
        Bill bill = createTestBill();
        BillItem item1 = addTestItem(bill, "Salad", new BigDecimal("30.00"), 1);
        BillItem item2 = addTestItem(bill, "Soup", new BigDecimal("20.00"), 1); // Total distributed: 50
        BillParticipant p1 = addTestParticipant(bill, "Eve");
        BillParticipant p2 = addTestParticipant(bill, "Frank");

        assignItemToParticipant(bill, item1, p1, 1);
        assignItemToParticipant(bill, item2, p2, 1);

        UpdateTaxServiceRequest taxServiceRequest = new UpdateTaxServiceRequest();
        taxServiceRequest.setTaxType(FeeType.AMOUNT);
        taxServiceRequest.setTaxValue(new BigDecimal("5.00")); // 5.00 tax
        taxServiceRequest.setServiceType(FeeType.AMOUNT);
        taxServiceRequest.setServiceValue(new BigDecimal("2.50")); // 2.50 service
        billService.updateTaxService(bill.getId().toString(), taxServiceRequest);

        SplitResultResponse result = billService.calculateSplit(bill.getId().toString());

        assertNotNull(result);
        // totalItemsDistributed = 50.00
        // tax = 5.00
        // service = 2.50
        // Grand Total = 50 + 5 + 2.50 = 57.50
        assertEquals(new BigDecimal("57.50").setScale(2, RoundingMode.HALF_UP), result.getGrandTotal());
        assertEquals(2, result.getResults().size());

        // Eve subtotal = 30.00
        // Frank subtotal = 20.00
        // Total Distributed Subtotal = 50.00
        // Eve tax = (30/50) * 5 = 3.00
        // Eve service = (30/50) * 2.50 = 1.50
        // Eve total = 30 + 3 + 1.50 = 34.50
        Map<String, Object> eveResult = result.getResults().stream()
                .filter(r -> "Eve".equals(r.get("participant")))
                .findFirst().orElseThrow();
        assertEquals(new BigDecimal("34.50").setScale(2, RoundingMode.HALF_UP), eveResult.get("amountToPay"));

        // Frank tax = (20/50) * 5 = 2.00
        // Frank service = (20/50) * 2.50 = 1.00
        // Frank total = 20 + 2 + 1.00 = 23.00
        Map<String, Object> frankResult = result.getResults().stream()
                .filter(r -> "Frank".equals(r.get("participant")))
                .findFirst().orElseThrow();
        assertEquals(new BigDecimal("23.00").setScale(2, RoundingMode.HALF_UP), frankResult.get("amountToPay"));
    }

    @Test
    @Transactional
    void testCalculateSplit_UnevenlyDivisibleItem() {
        Bill bill = createTestBill();
        BillItem item1 = addTestItem(bill, "Drink", new BigDecimal("10.00"), 3); // Ubah dari 1 menjadi 3
        BillParticipant p1 = addTestParticipant(bill, "Grace");
        BillParticipant p2 = addTestParticipant(bill, "Heidi");
        BillParticipant p3 = addTestParticipant(bill, "Ivan");

        assignItemToParticipant(bill, item1, p1, 1);
        assignItemToParticipant(bill, item1, p2, 1);
        assignItemToParticipant(bill, item1, p3, 1); // Split 10.00 among 3 parts

        SplitResultResponse result = billService.calculateSplit(bill.getId().toString());

        assertNotNull(result);
        assertEquals(3, result.getResults().size());
        assertEquals(new BigDecimal("30.00").setScale(2, RoundingMode.HALF_UP), result.getGrandTotal());

        // 30.00 / 3 = 10.00. Semua peserta membayar jumlah yang sama.

        List<Map<String, Object>> sortedResults = result.getResults().stream()
                .sorted(Comparator.comparing(r -> (String) r.get("participant")))
                .toList();

        assertEquals(new BigDecimal("10.00").setScale(2, RoundingMode.HALF_UP), sortedResults.get(0).get("amountToPay")); // Grace
        assertEquals(new BigDecimal("10.00").setScale(2, RoundingMode.HALF_UP), sortedResults.get(1).get("amountToPay")); // Heidi
        assertEquals(new BigDecimal("10.00").setScale(2, RoundingMode.HALF_UP), sortedResults.get(2).get("amountToPay")); // Ivan
    }

    @Test
    @Transactional
    void testCalculateSplit_ItemNotAssigned() {
        Bill bill = createTestBill();
        BillItem item1 = addTestItem(bill, "Assigned Item", new BigDecimal("50.00"), 1);
        BillItem item2 = addTestItem(bill, "Unassigned Item", new BigDecimal("20.00"), 1); // This item is not assigned
        BillParticipant p1 = addTestParticipant(bill, "Jack");

        assignItemToParticipant(bill, item1, p1, 1);

        SplitResultResponse result = billService.calculateSplit(bill.getId().toString());

        assertNotNull(result);
        // totalItemsRaw = 70.00 (sum of all items)
        // totalItemsDistributed = 50.00 (only assigned item)
        // GrandTotal should reflect only distributed items, as unassigned items are not "split"
        assertEquals(new BigDecimal("50.00").setScale(2, RoundingMode.HALF_UP), result.getGrandTotal());
        assertEquals(1, result.getResults().size());

        Map<String, Object> jackResult = result.getResults().stream()
                .filter(r -> "Jack".equals(r.get("participant")))
                .findFirst().orElseThrow();
        assertEquals(new BigDecimal("50.00").setScale(2, RoundingMode.HALF_UP), jackResult.get("amountToPay"));
    }

    @Test
    @Transactional
    void testCalculateSplit_NoItemsOrParticipants() {
        Bill bill = createTestBill();
        SplitResultResponse result = billService.calculateSplit(bill.getId().toString());

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result.getGrandTotal());
        assertTrue(result.getResults().isEmpty());
    }

    @Test
    @Transactional
    void testCalculateSplit_SpecificScenarioNoTaxService() {
        Bill bill = createTestBill();
        BillItem itemOriginal = addTestItem(bill, "Bakso Original", new BigDecimal("13000.00"), 3); // Total 39,000
        BillItem itemTelur = addTestItem(bill, "Bakso Telur", new BigDecimal("20000.00"), 1); // Total 20,000

        BillParticipant wahyu = addTestParticipant(bill, "Wahyu");
        BillParticipant aji = addTestParticipant(bill, "Aji");

        // Aji eats all 3 Bakso Original
        assignItemToParticipant(bill, itemOriginal, aji, 3);
        // Wahyu eats all 1 Bakso Telur
        assignItemToParticipant(bill, itemTelur, wahyu, 1);

        SplitResultResponse result = billService.calculateSplit(bill.getId().toString());

        assertNotNull(result);
        assertEquals(new BigDecimal("59000.00").setScale(2, RoundingMode.HALF_UP), result.getGrandTotal());
        assertEquals(2, result.getResults().size());

        Map<String, Object> ajiResult = result.getResults().stream()
                .filter(r -> "Aji".equals(r.get("participant")))
                .findFirst().orElseThrow();
        assertEquals(new BigDecimal("39000.00").setScale(2, RoundingMode.HALF_UP), ajiResult.get("amountToPay"));

        Map<String, Object> wahyuResult = result.getResults().stream()
                .filter(r -> "Wahyu".equals(r.get("participant")))
                .findFirst().orElseThrow();
        assertEquals(new BigDecimal("20000.00").setScale(2, RoundingMode.HALF_UP), wahyuResult.get("amountToPay"));
    }

    // New tests for assignItem modifications
    @Test
    @Transactional
    void testAssignItem_ReduceQuantity() {
        Bill bill = createTestBill();
        BillItem item = addTestItem(bill, "Nasi Goreng", new BigDecimal("25000.00"), 2);
        BillParticipant p1 = addTestParticipant(bill, "Budi");
        BillParticipant p2 = addTestParticipant(bill, "Ani");

        assignItemToParticipant(bill, item, p1, 1);
        assignItemToParticipant(bill, item, p2, 1);
        assertEquals(1, itemAssignmentRepository.findByBillItemIdAndParticipantId(item.getId(), p1.getId()).orElseThrow().getQuantityTaken());
        assertEquals(1, itemAssignmentRepository.findByBillItemIdAndParticipantId(item.getId(), p2.getId()).orElseThrow().getQuantityTaken());

        // Reduce Budi's quantity
        assignItemToParticipant(bill, item, p1, 0); // Should delete the assignment
        
        // Verify assignment for p1 is deleted
        assertFalse(itemAssignmentRepository.findByBillItemIdAndParticipantId(item.getId(), p1.getId()).isPresent());
        // Verify p2's assignment is still there
        assertTrue(itemAssignmentRepository.findByBillItemIdAndParticipantId(item.getId(), p2.getId()).isPresent());

        // Re-assign Budi with 1
        assignItemToParticipant(bill, item, p1, 1);
        assertEquals(1, itemAssignmentRepository.findByBillItemIdAndParticipantId(item.getId(), p1.getId()).orElseThrow().getQuantityTaken());
        assertEquals(1, itemAssignmentRepository.findByBillItemIdAndParticipantId(item.getId(), p2.getId()).orElseThrow().getQuantityTaken());
    }

    @Test
    @Transactional
    void testAssignItem_RemoveAssignmentBySettingQuantityToZero() {
        Bill bill = createTestBill();
        BillItem item = addTestItem(bill, "Sate Ayam", new BigDecimal("30000.00"), 1);
        BillParticipant p1 = addTestParticipant(bill, "Cici");

        assignItemToParticipant(bill, item, p1, 1);
        assertTrue(itemAssignmentRepository.findByBillItemIdAndParticipantId(item.getId(), p1.getId()).isPresent());

        assignItemToParticipant(bill, item, p1, 0); // Set quantity to 0 to remove

        assertFalse(itemAssignmentRepository.findByBillItemIdAndParticipantId(item.getId(), p1.getId()).isPresent());
    }

    @Test
    @Transactional
    void testAssignItem_NegativeQuantityThrowsException() {
        Bill bill = createTestBill();
        BillItem item = addTestItem(bill, "Es Teh", new BigDecimal("5000.00"), 1);
        BillParticipant p1 = addTestParticipant(bill, "Dede");

        Exception exception = assertThrows(BadRequestException.class, () -> {
            assignItemToParticipant(bill, item, p1, -1);
        });
        assertEquals("Quantity taken tidak bisa kurang dari 0", exception.getMessage());
    }

    @Test
    @Transactional
    void testAssignItem_ExceedsItemQuantityThrowsException() {
        Bill bill = createTestBill();
        BillItem item = addTestItem(bill, "Mie Ayam", new BigDecimal("15000.00"), 2); // Only 2 available
        BillParticipant p1 = addTestParticipant(bill, "Eka");
        BillParticipant p2 = addTestParticipant(bill, "Fina");

        assignItemToParticipant(bill, item, p1, 1);
        assignItemToParticipant(bill, item, p2, 1);
        
        // Both Eka and Fina have taken 1, total 2. Trying to take more should fail.
        BillParticipant p3 = addTestParticipant(bill, "Gita");
        Exception exception = assertThrows(BadRequestException.class, () -> {
            assignItemToParticipant(bill, item, p3, 1); // This would make total 3
        });
        assertTrue(exception.getMessage().contains("Total quantity taken untuk item 'Mie Ayam' melebihi quantity yang tersedia. Maksimal: 2"));

        // Change Eka's quantity to 2, total becomes 3 which exceeds 2
        Exception exception2 = assertThrows(BadRequestException.class, () -> {
            assignItemToParticipant(bill, item, p1, 2); // Eka takes 2, Fina takes 1 => Total 3
        });
        assertTrue(exception2.getMessage().contains("Total quantity taken untuk item 'Mie Ayam' melebihi quantity yang tersedia. Maksimal: 2"));
    }
}