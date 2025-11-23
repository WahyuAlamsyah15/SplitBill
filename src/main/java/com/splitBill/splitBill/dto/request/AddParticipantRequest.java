package com.splitBill.splitBill.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddParticipantRequest {
    @NotBlank(message = "Nama participant wajib diisi")
    private String name;
}