package com.vattima.bricklink.inventory.service;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PriceCalculatorServiceTest {
    @Test
    public void calculatePrice() {
        PriceCalculatorService priceCalculatorService = new PriceCalculatorService();
        double actualPrice = priceCalculatorService.calculatePrice(new double[]{});
        assertThat(actualPrice).isEqualTo(0.0d);
    }

}