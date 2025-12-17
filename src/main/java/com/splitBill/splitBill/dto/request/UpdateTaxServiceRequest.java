package com.splitBill.splitBill.dto.request;

import com.splitBill.splitBill.model.FeeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateTaxServiceRequest {

    @NotNull(message = "Tipe pajak (taxType) wajib diisi")
    private FeeType taxType;

    @NotNull(message = "Nilai pajak (taxValue) wajib diisi")
    @DecimalMin(value = "0.0", inclusive = true, message = "Nilai pajak tidak boleh negatif")
    private BigDecimal taxValue;

    @NotNull(message = "Tipe layanan (serviceType) wajib diisi")
    private FeeType serviceType;

    @NotNull(message = "Nilai layanan (serviceValue) wajib diisi")
    @DecimalMin(value = "0.0", inclusive = true, message = "Nilai layanan tidak boleh negatif")
    private BigDecimal serviceValue;
}