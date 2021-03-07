package com.vattima.bricklink.inventory.service;

import com.vattima.lego.inventory.pricing.PriceNotCalculableException;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.dto.BricklinkInventory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class PriceCalculatorServiceTest {
    @Test
    void calculatePrice() {
        PriceCalculatorService priceCalculatorService = new PriceCalculatorService(null);

        assertThatThrownBy(() -> {
            priceCalculatorService.calculatePrice(null, new double[]{});
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
        DescriptiveStatistics descriptiveStatistics2 = new DescriptiveStatistics();
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
        double[] prices = new double[]{5.67d, 9.45d, 15.23d, 11.50d, 28.34d, 36.15d, 88.01d, 17.71d, 15.92d, 29.51d};
        prices = Arrays.stream(prices).sorted().toArray();
        for (double d : prices) {
            log.info("[{}]", d);
        }
        PriceCalculatorService priceCalculatorService = new PriceCalculatorService(null);
        log.info("Price: [{}]", priceCalculatorService.calculatePrice(build("SL", "M"), prices));
        log.info("Price: [{}]", priceCalculatorService.calculatePrice(build("M", "M"), prices));
        log.info("Price: [{}]", priceCalculatorService.calculatePrice(build("E", "E"), prices));
        log.info("Price: [{}]", priceCalculatorService.calculatePrice(build("VG", "VG"), prices));
        log.info("Price: [{}]", priceCalculatorService.calculatePrice(build("G", "G"), prices));
        log.info("Price: [{}]", priceCalculatorService.calculatePrice(build("F", "F"), prices));
        log.info("Price: [{}]", priceCalculatorService.calculatePrice(build("P", "P"), prices));
        log.info("Price: [{}]", priceCalculatorService.calculatePrice(build("MS", "MS"), prices));


        prices = new Random().doubles(287314).map(d -> d * 100).sorted().toArray();
        priceCalculatorService = new PriceCalculatorService(null);
        double price = priceCalculatorService.calculatePrice(build("E", "E"), prices);
        log.info("Price: [{}]", price);
    }

    @Test
    void getPriceAdjustment_variousCombinations() {
        PriceCalculatorService priceCalculatorService = new PriceCalculatorService(null);
        double priceAdjustment = priceCalculatorService.getPriceAdjustment(build("SL", "M"));
        assertThat(priceAdjustment).isEqualTo(1.25d);
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
}