package com.splitBill.splitBill.controller;

import com.splitBill.splitBill.dto.request.*;
import com.splitBill.splitBill.dto.response.*;
import com.splitBill.splitBill.handler.ApiResponse;
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
        List<Map<String, Object>> billList = billService.getAllBills();
        return ResponseEntity.ok(ApiResponse.success("Daftar bill ditemukan", billList));
    }

    @GetMapping("/{billId}")
    public ApiResponse<BillResponse> getBillById(@PathVariable String billId) {
        BillResponse bill = billService.getBillById(billId);
        return ApiResponse.success("Bill ditemukan", bill);
    }
}