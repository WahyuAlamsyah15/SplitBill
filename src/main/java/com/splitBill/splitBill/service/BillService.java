package com.splitBill.splitBill.service;

import com.splitBill.splitBill.dto.request.*;
import com.splitBill.splitBill.dto.response.*;
import com.splitBill.splitBill.handler.BadRequestException;
import com.splitBill.splitBill.handler.ResourceNotFoundException;
import com.splitBill.splitBill.model.*;
import com.splitBill.splitBill.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final BillParticipantRepository participantRepository;
    private final ItemAssignmentRepository assignmentRepository;
    private final RestoRepository restoRepository;

    private String getCurrentTenantId() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getTenantDbName();
    }

    @Transactional
    public BillResponse createBill(CreateBillRequest request) {
        String tenantId = getCurrentTenantId();
        UUID restoUuid = UUID.fromString(request.getRestoId());

        Resto resto = restoRepository.findByIdAndTenantId(restoUuid, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Resto tidak ditemukan untuk tenant ini"));

        Bill bill = new Bill();
        bill.setResto(resto);
        bill.setNote(request.getNote());
        bill.setTenantId(tenantId);

        bill = billRepository.save(bill);

        return mapToBillResponse(bill);
    }

    @Transactional
    public ParticipantResponse addParticipant(String billId, AddParticipantRequest request) {
        String tenantId = getCurrentTenantId();
        Bill bill = billRepository.findByIdAndTenantId(UUID.fromString(billId), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill tidak ditemukan"));

        BillParticipant participant = new BillParticipant();
        participant.setBill(bill);
        participant.setName(request.getName());
        participant.setTenantId(tenantId);
        participant = participantRepository.save(participant);

        return mapToParticipantResponse(participant);
    }

    @Transactional
    public void assignItem(String billId, String itemId, String participantId, AssignItemRequest request) {
        String tenantId = getCurrentTenantId();
        Bill bill = billRepository.findByIdAndTenantId(UUID.fromString(billId), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill tidak ditemukan"));

        BillItem item = billItemRepository.findById(UUID.fromString(itemId))
                .orElseThrow(() -> new ResourceNotFoundException("Item tidak ditemukan"));

        BillParticipant participant = participantRepository.findById(UUID.fromString(participantId))
                .orElseThrow(() -> new ResourceNotFoundException("Participant tidak ditemukan"));

        if (!item.getBill().getId().equals(bill.getId())) {
            throw new BadRequestException("Item tidak termasuk dalam bill ini");
        }
        if (!participant.getBill().getId().equals(bill.getId())) {
            throw new BadRequestException("Participant tidak termasuk dalam bill ini");
        }

        UUID itemUuid = UUID.fromString(itemId);
        UUID participantUuid = UUID.fromString(participantId);

        ItemAssignment assignment = assignmentRepository
                .findByBillItemIdAndParticipantId(itemUuid, participantUuid)
                .orElse(new ItemAssignment());

        assignment.setBillItem(item);
        assignment.setParticipant(participant);
        assignment.setQuantityTaken(request.getQuantityTaken());
        assignment.setTenantId(tenantId);
        assignmentRepository.save(assignment);
    }

    @Transactional
    public BillResponse updateTaxService(String billId, UpdateTaxServiceRequest request) {
        String tenantId = getCurrentTenantId();
        Bill bill = billRepository.findByIdAndTenantId(UUID.fromString(billId), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill tidak ditemukan"));

        bill.setTaxPercent(request.getTaxPercent() != null ? request.getTaxPercent() : BigDecimal.ZERO);
        bill.setServicePercent(request.getServicePercent() != null ? request.getServicePercent() : BigDecimal.ZERO);

        bill = billRepository.save(bill);
        return mapToBillResponse(bill);
    }

    @Transactional(readOnly = true)
    public SplitResultResponse calculateSplit(String billId) {
        String tenantId = getCurrentTenantId();
        Bill bill = billRepository.findByIdAndTenantId(UUID.fromString(billId), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill tidak ditemukan"));

        List<ItemAssignment> assignments = assignmentRepository.findAll().stream()
                .filter(a -> a.getBillItem().getBill().getId().equals(bill.getId()))
                .toList();

        Map<UUID, Integer> itemTotalParts = new HashMap<>();
        Map<UUID, Map<UUID, Integer>> personPartsPerItem = new HashMap<>();

        for (ItemAssignment a : assignments) {
            UUID itemId = a.getBillItem().getId();
            UUID personId = a.getParticipant().getId();
            int parts = a.getQuantityTaken();

            itemTotalParts.merge(itemId, parts, Integer::sum);
            personPartsPerItem
                    .computeIfAbsent(itemId, k -> new HashMap<>())
                    .merge(personId, parts, Integer::sum);
        }

        Map<String, BigDecimal> personSubtotal = new HashMap<>();
        BigDecimal totalItems = BigDecimal.ZERO;

        for (BillItem item : bill.getItems()) {
            int totalParts = itemTotalParts.getOrDefault(item.getId(), 0);
            if (totalParts == 0) continue;

            BigDecimal pricePerPart = item.getPrice().divide(BigDecimal.valueOf(totalParts), 2, RoundingMode.HALF_UP);

            Map<UUID, Integer> personParts = personPartsPerItem.getOrDefault(item.getId(), new HashMap<>());
            for (BillParticipant p : bill.getParticipants()) {
                int parts = personParts.getOrDefault(p.getId(), 0);
                BigDecimal amount = pricePerPart.multiply(BigDecimal.valueOf(parts));
                personSubtotal.merge(p.getName(), amount, BigDecimal::add);
                totalItems = totalItems.add(amount);
            }
        }

        BigDecimal taxRate = bill.getTaxPercent() != null ? bill.getTaxPercent() : BigDecimal.ZERO;
        BigDecimal serviceRate = bill.getServicePercent() != null ? bill.getServicePercent() : BigDecimal.ZERO;
        BigDecimal totalTax = totalItems.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalService = totalItems.multiply(serviceRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = totalItems.add(totalTax).add(totalService);

        List<Map<String, Object>> results = new ArrayList<>();
        for (BillParticipant p : bill.getParticipants()) {
            BigDecimal subtotal = personSubtotal.getOrDefault(p.getName(), BigDecimal.ZERO);
            BigDecimal personTax = totalItems.compareTo(BigDecimal.ZERO) > 0
                    ? totalTax.multiply(subtotal).divide(totalItems, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal personService = totalItems.compareTo(BigDecimal.ZERO) > 0
                    ? totalService.multiply(subtotal).divide(totalItems, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            BigDecimal total = subtotal.add(personTax).add(personService).setScale(0, RoundingMode.HALF_UP);

            Map<String, Object> map = new HashMap<>();
            map.put("participant", p.getName());
            map.put("amountToPay", total);
            results.add(map);
        }

        SplitResultResponse resp = new SplitResultResponse();
        resp.setGrandTotal(grandTotal.setScale(0, RoundingMode.HALF_UP));
        resp.setResults(results);
        return resp;
    }

    public List<Map<String, Object>> getAllBills() {
        String tenantId = getCurrentTenantId();
        return billRepository.findAllWithRestoByTenantId(tenantId).stream()
                .map(bill -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", bill.getId().toString());
                    map.put("restoName", bill.getResto().getName());
                    map.put("note", bill.getNote() != null ? bill.getNote() : "");
                    map.put("createdAt", bill.getCreatedAt() != null ? bill.getCreatedAt().toString() : "");
                    return map;
                })
                .sorted((a, b) -> {
                    String dateA = (String) a.get("createdAt");
                    String dateB = (String) b.get("createdAt");
                    return dateB.compareTo(dateA); // terbaru di atas
                })
                .toList();
    }

    // Helper methods
    private BillResponse mapToBillResponse(Bill bill) {
        BillResponse res = new BillResponse();
        res.setId(bill.getId());
        
        if (bill.getResto() != null) { // The @Where clause on Resto handles soft deletes
            res.setRestoId(bill.getResto().getId());
            res.setRestoName(bill.getResto().getName());
        } else {
            res.setRestoId(null);
            res.setRestoName("Resto Dihapus");
        }

        res.setNote(bill.getNote());
        res.setTaxPercent(bill.getTaxPercent() != null ? bill.getTaxPercent() : BigDecimal.ZERO);
        res.setServicePercent(bill.getServicePercent() != null ? bill.getServicePercent() : BigDecimal.ZERO);

        if (bill.getItems() != null && !bill.getItems().isEmpty()) {
            List<ItemResponse> itemResponses = bill.getItems().stream()
                    .map(item -> {
                        ItemResponse ir = new ItemResponse();
                        ir.setId(item.getId());
                        ir.setName(item.getName());
                        ir.setPrice(item.getPrice());
                        ir.setQuantity(item.getQuantity());
                        ir.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                        return ir;
                    })
                    .collect(Collectors.toList());
            res.setItems(itemResponses);
        }

        return res;
    }

    private ParticipantResponse mapToParticipantResponse(BillParticipant p) {
        ParticipantResponse res = new ParticipantResponse();
        res.setId(p.getId());
        res.setName(p.getName());
        return res;
    }

    @Transactional(readOnly = true)
    public List<BillResponse> getByBillId(String billId) {
        String tenantId = getCurrentTenantId();
        return billRepository.findByIdAndTenantId(UUID.fromString(billId), tenantId)
                .stream()
                .map(this::mapToBillResponse)
                .toList();
    }
}