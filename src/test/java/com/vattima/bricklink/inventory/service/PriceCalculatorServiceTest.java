package com.vattima.bricklink.inventory.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class PriceCalculatorServiceTest {
    @Test
    public void calculatePrice() {
        PriceCalculatorService priceCalculatorService = new PriceCalculatorService();
        double actualPrice = priceCalculatorService.calculatePrice(new double[]{});
        assertThat(actualPrice).isEqualTo(0.0d);

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

}