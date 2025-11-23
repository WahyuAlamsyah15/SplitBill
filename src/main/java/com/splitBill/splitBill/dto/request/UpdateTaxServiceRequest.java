package com.splitBill.splitBill.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateTaxServiceRequest {
    @DecimalMin(value = "0.0", inclusive = true, message = "Tax tidak boleh negatif")
    private BigDecimal taxPercent = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", inclusive = true, message = "Service tidak boleh negatif")
    private BigDecimal servicePercent = BigDecimal.ZERO;
}