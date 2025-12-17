package com.splitBill.splitBill.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.splitBill.splitBill.dto.request.AddItemRequest;
import com.splitBill.splitBill.dto.request.UpdateItemRequest;
import com.splitBill.splitBill.dto.response.BillResponse;
import com.splitBill.splitBill.dto.response.ItemResponse;
import com.splitBill.splitBill.handler.ApiResponse;
import com.splitBill.splitBill.service.BillService;
import com.splitBill.splitBill.service.ItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // CREATE
    @PostMapping("/{billId}/items")
    public ApiResponse<ItemResponse> addItem(
            @PathVariable String billId,
            @Valid @RequestBody AddItemRequest request) {
        return ApiResponse.success("Item ditambahkan", itemService.addItem(billId, request));
    }

    // READ - GET ALL ITEMS BY BILL
    @GetMapping("/{billId}/items")
    public ApiResponse<List<ItemResponse>> getAll(@PathVariable String billId){
        return ApiResponse.success("Data berhasil ditampilkan", itemService.getAll(billId));
    }

    // READ - GET ITEM BY ID
    @GetMapping("/{billId}/items/{id}")
    public ApiResponse<ItemResponse> getById(
            @PathVariable String billId,
            @PathVariable String id) {
        return ApiResponse.success("Data berhasil ditemukan", itemService.getById(billId, id));
    }
    

    // UPDATE
    @PatchMapping("/{billId}/items/{id}")
    public ApiResponse<ItemResponse> patch(
            @PathVariable String billId,
            @PathVariable String id,
            @RequestBody UpdateItemRequest request) {
        return ApiResponse.success(
                "Data berhasil di edit",
                itemService.edit(billId, id, request)
        );
    }

    // DELETE
    @DeleteMapping("/{billId}/items/{id}")
    public ApiResponse<String> delete(
            @PathVariable String billId,
            @PathVariable String id) {
        itemService.delete(billId, id);
        return ApiResponse.success("Item berhasil dihapus", null);
    }
}
