@Test
    @Transactional
    void testCalculateSplit_SpecificScenarioNoTaxService() {
        Bill bill = createTestBill();
        BillItem itemOriginal = addTestItem(bill, "Bakso Original", new BigDecimal("13000.00"), 3); // Total 39,000
        BillItem itemTelur = addTestItem(bill, "Bakso Telur", new BigDecimal("20000.00"), 1); // Total 20,000

        BillParticipant wahyu = addTestParticipant(bill, "Wahyu");
        BillParticipant aji = addTestParticipant(bill, "Aji");

        // Aji eats all 3 Bakso Original
        assignItemToParticipant(bill, itemOriginal, aji, 3);
        // Wahyu eats all 1 Bakso Telur
        assignItemToParticipant(bill, itemTelur, wahyu, 1);

        SplitResultResponse result = billService.calculateSplit(bill.getId().toString());

        assertNotNull(result);
        assertEquals(new BigDecimal("59000.00").setScale(2, RoundingMode.HALF_UP), result.getGrandTotal());
        assertEquals(2, result.getResults().size());

        Map<String, Object> ajiResult = result.getResults().stream()
                .filter(r -> "Aji".equals(r.get("participant")))
                .findFirst().orElseThrow();
        assertEquals(new BigDecimal("39000.00").setScale(2, RoundingMode.HALF_UP), ajiResult.get("amountToPay"));

        Map<String, Object> wahyuResult = result.getResults().stream()
                .filter(r -> "Wahyu".equals(r.get("participant")))
                .findFirst().orElseThrow();
        assertEquals(new BigDecimal("20000.00").setScale(2, RoundingMode.HALF_UP), wahyuResult.get("amountToPay"));
    }