package com.splitBill.splitBill.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import com.splitBill.splitBill.dto.request.AddParticipantRequest;
import com.splitBill.splitBill.dto.request.UpdateParticipantRequest;
import com.splitBill.splitBill.dto.response.ParticipantResponse;
import com.splitBill.splitBill.handler.ApiResponse;
import com.splitBill.splitBill.service.ParticipantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;

    // CREATE
    @PostMapping("/{billId}/participants")
    public ApiResponse<ParticipantResponse> addParticipant(
            @PathVariable String billId,
            @Valid @RequestBody AddParticipantRequest request) {

        return ApiResponse.success(
                "Participant berhasil ditambahkan",
                participantService.addParticipant(billId, request)
        );
    }

    // READ - Get All Participants in Bill
    @GetMapping("/{billId}/participants")
    public ApiResponse<List<ParticipantResponse>> getAll(
            @PathVariable String billId) {

        return ApiResponse.success(
                "Data participant berhasil ditampilkan",
                participantService.getAllParticipants(billId)
        );
    }

    // READ - Get Participant by ID
    @GetMapping("/{billId}/participants/{id}")
    public ApiResponse<ParticipantResponse> getById(@PathVariable String billId, @PathVariable String id) {
        return ApiResponse.success(
                "Participant ditemukan",
                participantService.getById(id)
        );
    }

    // UPDATE
    @PutMapping("/{billId}/participants/{id}")
    public ApiResponse<ParticipantResponse> update(@PathVariable String billId,
            @PathVariable String id,@Valid @RequestBody UpdateParticipantRequest request) {

        return ApiResponse.success(
                "Participant berhasil diupdate",
                participantService.edit(billId, id, request)
        );
    }

    // DELETE
    @DeleteMapping("/{billId}/participants/{id}")
    public ApiResponse<String> delete(@PathVariable String billId, @PathVariable String id) {
        participantService.delete(billId, id);
        return ApiResponse.success(
                "Participant berhasil dihapus",
                "OK"
        );
    }
}
