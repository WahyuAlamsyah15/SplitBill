package com.splitBill.splitBill.controller;

import com.splitBill.splitBill.dto.request.*;
import com.splitBill.splitBill.dto.response.*;
import com.splitBill.splitBill.handler.ApiResponse;
import com.splitBill.splitBill.model.Bill;
import com.splitBill.splitBill.repository.BillRepository;
import com.splitBill.splitBill.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    private final BillRepository billRepository;

    @PostMapping
    public ApiResponse<BillResponse> createBill(@Valid @RequestBody CreateBillRequest request) {
        return ApiResponse.success("Bill berhasil dibuat", billService.createBill(request));
    } 


    @PostMapping("/{billId}/items/{itemId}/assign/{participantId}")
    public ApiResponse<String> assignItem(
            @PathVariable String billId,
            @PathVariable String itemId,
            @PathVariable String participantId,
            @Valid @RequestBody AssignItemRequest request) {
        billService.assignItem(billId, itemId, participantId, request);
        return ApiResponse.success("Item berhasil dialokasikan ke participant", null);
    }

    @PatchMapping("/{billId}/tax-service")
    public ApiResponse<BillResponse> updateTaxService(
            @PathVariable String billId,
            @Valid @RequestBody UpdateTaxServiceRequest request) {
        return ApiResponse.success("Tax & service berhasil diupdate", billService.updateTaxService(billId, request));
    }

    @PostMapping("/{billId}/calculate")
    public ApiResponse<SplitResultResponse> calculate(
            @PathVariable String billId) {
        return ApiResponse.success("Split berhasil dihitung!", billService.calculateSplit(billId));
    }

    @GetMapping("/{billId}/result")
    public ApiResponse<SplitResultResponse> getResult(
            @PathVariable String billId) {
        return ApiResponse.success("Hasil split bill", billService.calculateSplit(billId));
    }

    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllBills() {
        List<Map<String, Object>> billList = billRepository.findAllWithResto().stream()
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

        return ResponseEntity.ok(ApiResponse.success("Daftar bill ditemukan", billList));
    }
}