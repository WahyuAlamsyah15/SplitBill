package com.splitBill.splitBill.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBillRequest {
    @NotBlank(message = "restoId wajib diisi")
    private String restoId;

    private String note;
}