package com.splitBill.splitBill.controller;

import com.splitBill.splitBill.dto.request.CreateRestoRequest;
import com.splitBill.splitBill.dto.request.UpdateRestoRequest;
import com.splitBill.splitBill.dto.response.RestoResponse;
import com.splitBill.splitBill.handler.ApiResponse;
import com.splitBill.splitBill.handler.BadRequestException;
import com.splitBill.splitBill.service.RestoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restos")
@RequiredArgsConstructor
public class RestoController {

    private final RestoService restoService;

    @GetMapping
    public ApiResponse<List<RestoResponse>> getAllResto() {
        return ApiResponse.success("Daftar resto", restoService.getAllResto());
    }

    @PostMapping
    public ApiResponse<RestoResponse> createResto(@Valid @RequestBody CreateRestoRequest request) {
        RestoResponse resto = restoService.createResto(request.getName());
        return ApiResponse.success("Resto berhasil dibuat", resto);
    }

    @PutMapping("/{id}")
    public ApiResponse<RestoResponse> updateResto(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRestoRequest request) {
        RestoResponse updated = restoService.updateResto(id, request.getName().trim());
        return ApiResponse.success("Resto berhasil diupdate", updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteResto(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "false") boolean force) {
        if (force) {
            restoService.deleteRestoWithAllBills(id);
            return ApiResponse.success("Resto dan semua bill terkait berhasil dihapus secara permanen", null);
        } else {
            restoService.softDeleteResto(id);
            return ApiResponse.success("Resto berhasil dihapus (soft delete)", null);
        }
    }
}