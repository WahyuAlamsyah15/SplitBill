package com.splitBill.splitBill.dto.request;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateParticipantRequest{
    private UUID id;
    private String name;
}
