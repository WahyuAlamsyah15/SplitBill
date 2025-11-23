package com.splitBill.splitBill.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class ParticipantResponse {
    private UUID id;
    private String name;
}