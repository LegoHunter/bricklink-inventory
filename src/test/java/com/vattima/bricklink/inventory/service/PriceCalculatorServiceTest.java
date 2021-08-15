package com.vattima.bricklink.inventory.service;

import com.vattima.lego.inventory.pricing.PriceNotCalculableException;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.dto.BricklinkInventory;
import net.bricklink.data.lego.dto.BricklinkSaleItem;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@Slf4j
class PriceCalculatorServiceTest {
    @Test
    void calculatePrice() {
        PriceCalculatorService priceCalculatorService = new PriceCalculatorService(null);

        assertThatThrownBy(() -> {
            priceCalculatorService.calculatePrice(null, Collections.emptyList());
        }).isInstanceOf(PriceNotCalculableException.class)
          .hasMessageContaining("There are no prices");

        double[] prices = new double[]{5.67d, 9.45d, 2.17d, 11.50d, 8.34d, 6.15d, 10.01d, 7.71d, 5.92d, 9.51d};
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
        for (double v : prices) {
            descriptiveStatistics.addValue(v);
        }

        double mean = descriptiveStatistics.getMean();
        double geometricMean = descriptiveStatistics.getGeometricMean();
        double median = descriptiveStatistics.getPercentile(50);
        double standardDeviation = descriptiveStatistics.getStandardDeviation();
        log.info("mean [{}]", mean);
        log.info("geometric mean [{}]", geometricMean);
        log.info("median [{}]", median);
        log.info("standard deviation [{}]", standardDeviation);


        double[] prices2 = new double[]{5.67d, 9.45d, 15.23d, 11.50d, 28.34d, 36.15d, 88.01d, 17.71d, 15.92d, 29.51d};
        DescriptiveStatistics descriptiveStatistics2  = new DescriptiveStatistics();
        for (double v : prices2) {
            descriptiveStatistics2.addValue(v);
        }

        double mean2 = descriptiveStatistics2.getMean();
        double geometricMean2 = descriptiveStatistics2.getGeometricMean();
        double median2 = descriptiveStatistics2.getPercentile(50);
        double standardDeviation2 = descriptiveStatistics2.getStandardDeviation();
        log.info("mean [{}]", mean2);
        log.info("geometric mean [{}]", geometricMean2);
        log.info("median [{}]", median2);
        log.info("standard deviation [{}]", standardDeviation2);
    }

    @Test
    void calculatePrice_usingService() {
        List<BricklinkSaleItem> bricklinkSaleItems = List.of(bsi(5.67d, "US"),
                bsi(9.45d,  "US"),
                bsi(15.23d, "US"),
                bsi(11.50d, "US"),
                bsi(28.34d, "US"),
                bsi(36.15d, "US"),
                bsi(88.01d, "US"),
                bsi(17.71d, "US"),
                bsi(15.92d, "US"),
                bsi(29.51d, "US"));
        PriceCalculatorService priceCalculatorService = new PriceCalculatorService(null);
        log.info("Price: [{}]", priceCalculatorService.calculatePrice(build("SL", "M"),  bricklinkSaleItems));
        log.info("Price: [{}]", priceCalculatorService.calculatePrice(build("M", "M"),   bricklinkSaleItems));
        log.info("Price: [{}]", priceCalculatorService.calculatePrice(build("E", "E"),   bricklinkSaleItems));
        log.info("Price: [{}]", priceCalculatorService.calculatePrice(build("VG", "VG"), bricklinkSaleItems));
        log.info("Price: [{}]", priceCalculatorService.calculatePrice(build("G", "G"),   bricklinkSaleItems));
        log.info("Price: [{}]", priceCalculatorService.calculatePrice(build("F", "F"),   bricklinkSaleItems));
        log.info("Price: [{}]", priceCalculatorService.calculatePrice(build("P", "P"),   bricklinkSaleItems));
        log.info("Price: [{}]", priceCalculatorService.calculatePrice(build("MS", "MS"), bricklinkSaleItems));


        List<BricklinkSaleItem> bsiList = new ArrayList<>();
        new Random().doubles(287314).map(d -> d * 100).sorted().forEach(d -> bsiList.add(bsi(d, "US")));
        priceCalculatorService = new PriceCalculatorService(null);
        double price = priceCalculatorService.calculatePrice(build("E", "E"), bsiList);
        log.info("Price: [{}]", price);
    }

    @Test
    void getPriceAdjustment_variousCombinations() {
        PriceCalculatorService priceCalculatorService = new PriceCalculatorService(null);
        double priceAdjustment = priceCalculatorService.getPriceAdjustment(build("SL", "M"));
        assertThat(priceAdjustment).isEqualTo(1.25d);
    }

    @Test
    void calculatePriceUsingOnePrice() {
        PriceCalculatorService priceCalculatorService = new PriceCalculatorService(null);
        assertThat(priceCalculatorService.calculatePriceUsingOnePrice(null, List.of(bsi(1.0, "US")))).isEqualTo(0.97, within(0.01));
        assertThat(priceCalculatorService.calculatePriceUsingOnePrice(null, List.of(bsi(32.0, "US")))).isEqualTo(31.0, within(0.01));
        assertThat(priceCalculatorService.calculatePriceUsingOnePrice(null, List.of(bsi(33.0, "US")))).isEqualTo(32.0, within(0.01));
        assertThat(priceCalculatorService.calculatePriceUsingOnePrice(null, List.of(bsi(50.0, "US")))).isEqualTo(48.5, within(0.01));
        assertThat(priceCalculatorService.calculatePriceUsingOnePrice(null, List.of(bsi(100.0, "US")))).isEqualTo(97, within(0.01));
        assertThat(priceCalculatorService.calculatePriceUsingOnePrice(null, List.of(bsi(200.0, "US")))).isEqualTo(194, within(0.01));
        assertThat(priceCalculatorService.calculatePriceUsingOnePrice(null, List.of(bsi(300.0, "US")))).isEqualTo(291, within(0.01));
        assertThat(priceCalculatorService.calculatePriceUsingOnePrice(null, List.of(bsi(300.0, "US")))).isEqualTo(291, within(0.01));
        assertThat(priceCalculatorService.calculatePriceUsingOnePrice(null, List.of(bsi(330.0, "US")))).isEqualTo(320.1, within(0.01));
        assertThat(priceCalculatorService.calculatePriceUsingOnePrice(null, List.of(bsi(331.0, "US")))).isEqualTo(321.07, within(0.01));
        assertThat(priceCalculatorService.calculatePriceUsingOnePrice(null, List.of(bsi(332.0, "US")))).isEqualTo(322.04, within(0.01));
        assertThat(priceCalculatorService.calculatePriceUsingOnePrice(null, List.of(bsi(333.0, "US")))).isEqualTo(323.01, within(0.01));
        assertThat(priceCalculatorService.calculatePriceUsingOnePrice(null, List.of(bsi(334.0, "US")))).isEqualTo(324, within(0.01));
        assertThat(priceCalculatorService.calculatePriceUsingOnePrice(null, List.of(bsi(335.0, "US")))).isEqualTo(325, within(0.01));
        assertThat(priceCalculatorService.calculatePriceUsingOnePrice(null, List.of(bsi(336.0, "US")))).isEqualTo(326, within(0.01));
        assertThat(priceCalculatorService.calculatePriceUsingOnePrice(null, List.of(bsi(500.0, "US")))).isEqualTo(490, within(0.01));
        assertThat(priceCalculatorService.calculatePriceUsingOnePrice(null, List.of(bsi(1000.0, "US")))).isEqualTo(990, within(0.01));
    }

    private BricklinkInventory build(final String boxConditionCode, final String instructionsConditionCode) {
//        SL	Sealed
//        M	Mint
//        E	Excellent
//        VG	Very Good
//        G	Good
//        P	Poor
//        NA	Not Applicable
//        F	Fair
//        MS	Missing
//        CC	Color Copy
//        BW	Black & White Copy
        BricklinkInventory bricklinkInventory = new BricklinkInventory();
        bricklinkInventory.setBoxConditionCode(boxConditionCode);
        bricklinkInventory.setInstructionsConditionCode(instructionsConditionCode);
        return bricklinkInventory;
    }

    private BricklinkSaleItem bsi(double unitPrice, String countryCode) {
        BricklinkSaleItem bricklinkSaleItem = new BricklinkSaleItem();
        bricklinkSaleItem.setUnitPrice(unitPrice);
        bricklinkSaleItem.setCountryCode(countryCode);
        return bricklinkSaleItem;
    }
}