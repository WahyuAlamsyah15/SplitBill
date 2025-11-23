package com.splitBill.splitBill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SplitBillApplication {

    public static void main(String[] args) {
        SpringApplication.run(SplitBillApplication.class, args);
        System.out.println("SplitBill Resto Backend BERHASIL JALAN di PORT 7000!");
        System.out.println("Swagger UI  → http://localhost:7000/swagger-ui.html");
        System.out.println("API Docs    → http://localhost:7000/api-docs");
    }
}