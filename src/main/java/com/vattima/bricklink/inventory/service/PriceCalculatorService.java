package com.vattima.bricklink.inventory.service;

import com.vattima.lego.inventory.pricing.PriceNotCalculableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.dao.BricklinkSaleItemDao;
import net.bricklink.data.lego.dto.BricklinkInventory;
import net.bricklink.data.lego.dto.BricklinkSaleItem;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class PriceCalculatorService {
    private static final Map<String, Double> instructionsConditionPriceAdjustmentTable = new HashMap<>();
    private static final Map<String, Double> boxConditionPriceAdjustmentTable = new HashMap<>();

    static {
        instructionsConditionPriceAdjustmentTable.put("M",  1.2d);
        instructionsConditionPriceAdjustmentTable.put("E",  1.0d);
        instructionsConditionPriceAdjustmentTable.put("VG", 0.95d);
        instructionsConditionPriceAdjustmentTable.put("G",  0.9d);
        instructionsConditionPriceAdjustmentTable.put("F",  0.8d);
        instructionsConditionPriceAdjustmentTable.put("P",  0.7d);
        instructionsConditionPriceAdjustmentTable.put("CC", 0.5d);
        instructionsConditionPriceAdjustmentTable.put("BW", 0.45d);
        instructionsConditionPriceAdjustmentTable.put("MS", 0.4d);
        instructionsConditionPriceAdjustmentTable.put("NA", 1.0d);

        boxConditionPriceAdjustmentTable.put("SL", 1.3d);
        boxConditionPriceAdjustmentTable.put("M",  1.2d);
        boxConditionPriceAdjustmentTable.put("E",  1.0d);
        boxConditionPriceAdjustmentTable.put("VG", 0.95d);
        boxConditionPriceAdjustmentTable.put("G",  0.9d);
        boxConditionPriceAdjustmentTable.put("F",  0.85d);
        boxConditionPriceAdjustmentTable.put("P",  0.70d);
        boxConditionPriceAdjustmentTable.put("MS", 0.5d);
        boxConditionPriceAdjustmentTable.put("NA", 1.0d);
    }

    private final BricklinkSaleItemDao bricklinkSaleItemDao;

    public double calculatePrice(final BricklinkInventory bricklinkInventory) {
        double price = 0.0d;
        double[] newPrices = getNewPrices(bricklinkInventory.getBlItemId(), bricklinkInventory.getCompleteness());
        double[] usedPrices = getUsedPrices(bricklinkInventory.getBlItemId(), bricklinkInventory.getCompleteness());

        if ("N".equals(bricklinkInventory.getNewOrUsed())) {
            price = calculatePrice(bricklinkInventory, newPrices);
        } else {
            price = calculatePrice(bricklinkInventory, usedPrices);
        }
        return (double)Math.round(price * 100)/100d;
    }

    public double[] getNewPrices(final Long blItemId, final String completeness) {
        return getPrices(blItemId, "N", completeness);
    }

    public double[] getUsedPrices(final Long blItemId, final String completeness) {
        return getPrices(blItemId, "U", completeness);
    }

    public double[] getPrices(final Long blItemId, final String newOrUsed, final String completeness) {
        return bricklinkSaleItemDao.getPricesForItem(blItemId, newOrUsed, completeness)
                                   .stream()
                                   .filter(bi -> bi.getStatus().equals("C"))
                                   .sorted(Comparator.comparing(BricklinkSaleItem::getUnitPrice))
                                   .mapToDouble(BricklinkSaleItem::getUnitPrice)
                                   .toArray();
    }

    public double calculatePrice(final BricklinkInventory bricklinkInventory, double[] prices) {
        double price;
        int pricesCount = prices.length;
        if (pricesCount > 2) {
            price = calculatePriceUsingMoreThanTwoPrices(bricklinkInventory, prices);
        } else if (pricesCount > 1) {
            price = calculatePriceUsingTwoPrices(bricklinkInventory, prices);
        } else if (pricesCount > 0) {
            price = calculatePriceUsingOnePrice(bricklinkInventory, prices);
        } else {
            throw new PriceNotCalculableException(bricklinkInventory, "There are no prices with which to calculate a price");
        }
        return adjustPrice(bricklinkInventory, price);
    }

    private double calculatePriceUsingTwoPrices(final BricklinkInventory bricklinkInventory, double[] prices) {
        double lowPrice = prices[0];
        double highPrice = prices[1];
        return lowPrice + (highPrice - lowPrice) * 0.75d;
    }

    private double calculatePriceUsingOnePrice(final BricklinkInventory bricklinkInventory, double[] prices) {
        double price = prices[0];
        return price * 0.85d;
    }

    private double calculatePriceUsingMoreThanTwoPrices(final BricklinkInventory bricklinkInventory, double[] prices) {
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
        if ((prices[prices.length - 1] / prices[prices.length - 2]) > 3) {
            throw new PriceNotCalculableException(bricklinkInventory, "["+bricklinkInventory.getNewOrUsed()+" "+bricklinkInventory.getCompleteness()+" "+bricklinkInventory.getBlItemNo()+" "+bricklinkInventory.getBlItemId()+"] Delta between last two prices is too high [" + prices[prices.length - 1] + "], [" + prices[prices.length - 2] + "]");
        }
        Arrays.stream(prices)
              .forEach(descriptiveStatistics::addValue);
        double mean = descriptiveStatistics.getMean();
        double standardDeviation = descriptiveStatistics.getStandardDeviation();
        return mean + standardDeviation;
    }

    public double adjustPrice(final BricklinkInventory bricklinkInventory, final double price) {
        return price * getPriceAdjustment(bricklinkInventory);
    }

    public double getPriceAdjustment(final BricklinkInventory bricklinkInventory) {
        double instructionsPriceAdjustment = getPriceAdjustmentForInstructions(bricklinkInventory);
        double boxPriceAdjustment = getPriceAdjustmentForBox(bricklinkInventory);
        return (instructionsPriceAdjustment + boxPriceAdjustment)/2.0d;
    }

    public double getPriceAdjustmentForInstructions(final BricklinkInventory bricklinkInventory) {
        return getPriceAdjustment(instructionsConditionPriceAdjustmentTable, bricklinkInventory.getInstructionsConditionCode(), 1.0d);
    }

    public double getPriceAdjustmentForBox(final BricklinkInventory bricklinkInventory) {
        return getPriceAdjustment(boxConditionPriceAdjustmentTable, bricklinkInventory.getBoxConditionCode(), 1.0d);
    }

    public double getPriceAdjustment(final Map<String, Double> adjustmentTable, final String conditionCode, final double _default) {
        return Optional.ofNullable(adjustmentTable.get(conditionCode)).orElse(_default);
    }
}
