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
    public ApiResponse<RestoResponse> createResto(@RequestBody CreateRestoRequest request) {
        RestoResponse resto = restoService.createResto(request.getName());
        return ApiResponse.success("Resto berhasil dibuat", resto);
    }

    @PutMapping("/{id}")
    public ApiResponse<RestoResponse> updateResto(
            @PathVariable UUID id,
            @RequestBody UpdateRestoRequest request) {

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Nama resto tidak boleh kosong");
        }

        RestoResponse updated = restoService.updateResto(id, request.getName().trim());
        return ApiResponse.success("Resto berhasil diupdate", updated);
    }

    @DeleteMapping
    public ApiResponse<String> deleteResto(@PathVariable UUID id) {
        restoService.softDeleteResto(id);
        return ApiResponse.success("Resto berhasil dihapus", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteRestoWithAllBills(@PathVariable UUID id) {
        restoService.deleteRestoWithAllBills(id);
        return ApiResponse.success("Resto berhasil dihapus");
    }
}