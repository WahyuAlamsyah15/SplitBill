package com.splitBill.splitBill.dto.temp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StagedRegistration {
    private String username;
    private String email;
    private String hashedPassword;
}
