package com.splitBill.splitBill.dto.response;

import com.splitBill.splitBill.model.FeeType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class BillResponse {
    private UUID id;
    private UUID restoId;
    private String note;
    private String restoName;
    private List<ItemResponse> items;
    private FeeType taxType;
    private BigDecimal taxValue;
    private FeeType serviceType;
    private BigDecimal serviceValue;
}