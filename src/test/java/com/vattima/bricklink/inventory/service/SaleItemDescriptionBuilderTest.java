package com.vattima.bricklink.inventory.service;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SaleItemDescriptionBuilderTest {

    @Test
    public void buildDescription_returnsNonNull() {
        SaleItemDescriptionBuilder saleItemDescriptionBuilder = new SaleItemDescriptionBuilder();
        String description = saleItemDescriptionBuilder.buildDescription();
        assertThat(description).isNotNull();
    }
}