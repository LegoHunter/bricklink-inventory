package com.vattima.bricklink.inventory.service;

import net.bricklink.data.lego.dto.BricklinkInventory;
import org.junit.Test;

import java.util.Optional;

import static com.vattima.bricklink.inventory.service.SaleItemDescriptionBuilder.ConditionDecoder;
import static org.assertj.core.api.Assertions.assertThat;

public class SaleItemDescriptionBuilderTest {

    @Test
    public void buildDescription_returnsNonNull() {
        SaleItemDescriptionBuilder saleItemDescriptionBuilder = new SaleItemDescriptionBuilder();

        BricklinkInventory bricklinkInventory = new BricklinkInventory();
        String description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);

        assertThat(description).isEmpty();
    }

    @Test
    public void buildDescription_withBoxConditionCode_containsBoxDescription() {
        SaleItemDescriptionBuilder saleItemDescriptionBuilder = new SaleItemDescriptionBuilder();

        BricklinkInventory bricklinkInventory = new BricklinkInventory();
        bricklinkInventory.setBoxConditionCode("M");
        String description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);
        assertThat(description).contains("Box: Mint");

        bricklinkInventory.setBoxConditionCode("E");
        description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);
        assertThat(description).contains("Box: Excellent");
    }

    @Test
    public void buildDescription_withSealedAndBoxConditionCode_containsBoxDescription() {
        SaleItemDescriptionBuilder saleItemDescriptionBuilder = new SaleItemDescriptionBuilder();

        BricklinkInventory bricklinkInventory = new BricklinkInventory();
        bricklinkInventory.setSealed(true);
        bricklinkInventory.setBoxConditionCode("E");
        String description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);
        assertThat(description).contains("Box: Excellent");
        assertThat(description).contains("Sealed");
    }

    @Test
    public void buildDescription_withExtraDescription() {
        SaleItemDescriptionBuilder saleItemDescriptionBuilder = new SaleItemDescriptionBuilder();

        BricklinkInventory bricklinkInventory = new BricklinkInventory();
        bricklinkInventory.setExtraDescription("This is an awesome set!");
        bricklinkInventory.setSealed(true);
        bricklinkInventory.setBoxConditionCode("E");
        bricklinkInventory.setInstructionsConditionCode("M");
        String description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);
        assertThat(description).contains("This is an awesome set!");
        assertThat(description).contains("Box: Excellent");
        assertThat(description).contains("Sealed");
    }

    @Test
    public void buildDescription_withInstructionsConditionCode_containsInstructionsDescription() {
        SaleItemDescriptionBuilder saleItemDescriptionBuilder = new SaleItemDescriptionBuilder();

        BricklinkInventory bricklinkInventory = new BricklinkInventory();
        bricklinkInventory.setInstructionsConditionCode("M");
        String description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);
        assertThat(description).contains("Instructions: Mint");

        bricklinkInventory.setInstructionsConditionCode("MS");
        description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);
        assertThat(description).contains("Instructions: Missing");

        bricklinkInventory.setInstructionsConditionCode("CC");
        description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);
        assertThat(description).contains("Instructions: Color Copy");

        bricklinkInventory.setInstructionsConditionCode("BW");
        description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);
        assertThat(description).contains("Instructions: Black & White Copy");
    }

    @Test
    public void buildDescription_withBoxAndInstructionsConditionCode_containsBoxAndInstructionsDescription() {
        SaleItemDescriptionBuilder saleItemDescriptionBuilder = new SaleItemDescriptionBuilder();

        BricklinkInventory bricklinkInventory = new BricklinkInventory();
        bricklinkInventory.setBoxConditionCode("E");
        bricklinkInventory.setInstructionsConditionCode("VG");
        String description = saleItemDescriptionBuilder.buildDescription(bricklinkInventory);
        assertThat(description).contains("Box: Excellent");
        assertThat(description).contains("Instructions: Very Good");
    }

    @Test
    public void conditionDecoder_returnsCondition_forKnownValue() {
        ConditionDecoder conditionDecoder = new ConditionDecoder();
        Optional<String> condition = conditionDecoder.decode("M");
        assertThat(condition).isPresent().get().isEqualTo("Mint");
    }

    @Test
    public void conditionDecoder_returnsNotPresent_forUnknownValue() {
        ConditionDecoder conditionDecoder = new ConditionDecoder();
        Optional<String> condition = conditionDecoder.decode("XX");
        assertThat(condition).isNotPresent();
    }
}