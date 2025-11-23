package com.splitBill.splitBill.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class SplitResultResponse {
    private BigDecimal totalBeforeTax;
    private BigDecimal taxAmount;
    private BigDecimal serviceAmount;
    private BigDecimal grandTotal;
    private List<Map<String, Object>> results; // nama → amountToPay
}