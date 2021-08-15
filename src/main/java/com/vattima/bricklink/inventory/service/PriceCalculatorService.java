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
import java.util.stream.Collectors;

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
        double price = Double.NaN;
        if (!bricklinkInventory.getFixedPrice()) {
            List<BricklinkSaleItem> newPrices = getNewPrices(bricklinkInventory.getBlItemId(), bricklinkInventory.getCompleteness());
            List<BricklinkSaleItem> usedPrices = getUsedPrices(bricklinkInventory.getBlItemId(), bricklinkInventory.getCompleteness());

            if ("N".equals(bricklinkInventory.getNewOrUsed())) {
                price = calculatePriceForNew(bricklinkInventory, newPrices);
            } else {
                price = calculatePriceForUsed(bricklinkInventory, usedPrices);
            }
        }
        return (double) Math.round(price * 100) / 100d;
    }

    public List<BricklinkSaleItem> getNewPrices(final Long blItemId, final String completeness) {
        return getBrinklinkSaleItems(blItemId, "N", completeness);
    }

    public List<BricklinkSaleItem> getUsedPrices(final Long blItemId, final String completeness) {
        return getBrinklinkSaleItems(blItemId, "U", completeness);
    }

    public double[] getPrices(final Long blItemId, final String newOrUsed, final String completeness) {
        return bricklinkSaleItemDao.getBrinklinkSaleItems(blItemId, newOrUsed, completeness)
                                   .stream()
                                   .filter(bi -> bi.getStatus()
                                                   .equals("C"))
                                   .sorted(Comparator.comparing(BricklinkSaleItem::getUnitPrice))
                                   .mapToDouble(BricklinkSaleItem::getUnitPrice)
                                   .toArray();
    }

    public List<BricklinkSaleItem> getBrinklinkSaleItems(final Long blItemId, final String newOrUsed, final String completeness) {
        return bricklinkSaleItemDao.getBrinklinkSaleItems(blItemId, newOrUsed, completeness)
                                   .stream()
                                   .filter(bi -> bi.getStatus()
                                                   .equals("C"))
                                   .sorted(Comparator.comparing(BricklinkSaleItem::getUnitPrice))
                                   .collect(Collectors.toList());
    }

    public double calculatePriceForNew(final BricklinkInventory bricklinkInventory, List<BricklinkSaleItem> saleItems) {
        double price;
        if (saleItems.size() > 2) {
            price = calculatePriceForNewSealedUsingMoreThanTwoPrices(bricklinkInventory, saleItems);
        } else if (saleItems.size() > 1) {
            price = calculatePriceUsingTwoPrices(bricklinkInventory, saleItems);
        } else if (saleItems.size() > 0) {
            price = calculatePriceUsingOnePrice(bricklinkInventory, saleItems);
        } else {
            throw new PriceNotCalculableException(bricklinkInventory, "There are no prices with which to calculate a price");
        }
        return adjustPrice(bricklinkInventory, price);
    }

    public double calculatePriceForUsed(final BricklinkInventory bricklinkInventory, List<BricklinkSaleItem> saleItems) {
        double price;
        if (saleItems.size() > 2) {
            price = calculatePriceUsingMoreThanTwoPrices(bricklinkInventory, saleItems);
        } else if (saleItems.size() > 1) {
            price = calculatePriceUsingTwoPrices(bricklinkInventory, saleItems);
        } else if (saleItems.size() > 0) {
            price = calculatePriceUsingOnePrice(bricklinkInventory, saleItems);
        } else {
            throw new PriceNotCalculableException(bricklinkInventory, "There are no prices with which to calculate a price");
        }
        return adjustPrice(bricklinkInventory, price);
    }

    public double calculatePrice(final BricklinkInventory bricklinkInventory, List<BricklinkSaleItem> saleItems) {
        double price;
        if (saleItems.size() > 2) {
            price = calculatePriceUsingMoreThanTwoPrices(bricklinkInventory, saleItems);
        } else if (saleItems.size() > 1) {
            price = calculatePriceUsingTwoPrices(bricklinkInventory, saleItems);
        } else if (saleItems.size() > 0) {
            price = calculatePriceUsingOnePrice(bricklinkInventory, saleItems);
        } else {
            throw new PriceNotCalculableException(bricklinkInventory, "There are no prices with which to calculate a price");
        }
        return adjustPrice(bricklinkInventory, price);
    }

    double calculatePriceUsingTwoPrices(final BricklinkInventory bricklinkInventory, List<BricklinkSaleItem> saleItems) {
        double[] prices = saleItems.stream()
                                   .mapToDouble(BricklinkSaleItem::getUnitPrice)
                                   .toArray();
        double lowPrice = prices[0];
        double highPrice = prices[1];
        return lowPrice + (highPrice - lowPrice) * 0.75d;
    }

    double calculatePriceUsingOnePrice(final BricklinkInventory bricklinkInventory, List<BricklinkSaleItem> saleItems) {
        double[] prices = saleItems.stream()
                                   .mapToDouble(BricklinkSaleItem::getUnitPrice)
                                   .toArray();
        double price = prices[0];

        double priceMinus3PercentUpTo10Dollars = price - Math.min(price * 0.03d, 10);
        double priceMinusOneDollar = Math.max(price - 1.0d, 1.0d);
        return Math.min(priceMinus3PercentUpTo10Dollars, priceMinusOneDollar);
    }

    private double calculatePriceForNewSealedUsingMoreThanTwoPrices(final BricklinkInventory bricklinkInventory, List<BricklinkSaleItem> saleItems) {
        List<BricklinkSaleItem> usSaleItems =
                saleItems.stream()
                         .filter(bsi -> "US".equals(bsi.getCountryCode()))
                         .filter(bsi -> "N".equals(bsi.getNewOrUsed()))
                         .filter(bsi -> List.of("S", "C")
                                            .contains(bsi.getCompleteness()))
                         .collect(Collectors.toList());

        List<BricklinkSaleItem> foreignSaleItems =
                saleItems.stream()
                         .filter(bsi -> !"US".equals(bsi.getCountryCode()))
                         .filter(bsi -> "N".equals(bsi.getNewOrUsed()))
                         .filter(bsi -> List.of("S", "C")
                                            .contains(bsi.getCompleteness()))
                         .collect(Collectors.toList());

        if (usSaleItems.isEmpty()) {
            log.info("Calculating price for {} among {} non US sets available", bricklinkInventory.getBlItemNo(),  foreignSaleItems.size());
            double price;
            if (foreignSaleItems.size() > 2) {
                double[] prices = saleItems.stream()
                                           .mapToDouble(BricklinkSaleItem::getUnitPrice)
                                           .toArray();
                DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
                if ((prices[prices.length - 1] / prices[prices.length - 2]) > 3) {
                    throw new PriceNotCalculableException(bricklinkInventory, "[" + bricklinkInventory.getNewOrUsed() + " " + bricklinkInventory.getCompleteness() + " " + bricklinkInventory.getBlItemNo() + " " + bricklinkInventory.getBlItemId() + "] Delta between last two prices is too high [" + prices[prices.length - 1] + "], [" + prices[prices.length - 2] + "]");
                }
                Arrays.stream(prices)
                      .forEach(descriptiveStatistics::addValue);
                double mean = descriptiveStatistics.getMean();
                double standardDeviation = descriptiveStatistics.getStandardDeviation();
                return mean + standardDeviation;
            } else if (foreignSaleItems.size() > 1) {
                price = calculatePriceUsingTwoPrices(bricklinkInventory, foreignSaleItems);
            } else if (foreignSaleItems.size() > 0) {
                price = calculatePriceUsingOnePrice(bricklinkInventory, foreignSaleItems);
            } else {
                throw new PriceNotCalculableException(bricklinkInventory, "There are no prices with which to calculate a price");
            }
            return adjustPrice(bricklinkInventory, price);

        } else {
            log.info("Calculating lowest price for {} based on {} US sets available", bricklinkInventory.getBlItemNo(),  usSaleItems.size());
            List<BricklinkSaleItem> lowestPriceUsSaleItemListOfOne =
                    usSaleItems.stream()
                                    .min(Comparator.comparing(BricklinkSaleItem::getUnitPrice))
                                    .map(bsi -> List.of(bsi))
                                    .orElseThrow(NoSuchElementException::new);
            return calculatePriceForNew(bricklinkInventory, lowestPriceUsSaleItemListOfOne);
        }
    }

    private double calculatePriceUsingMoreThanTwoPrices(final BricklinkInventory bricklinkInventory, List<BricklinkSaleItem> saleItems) {
        double[] prices = saleItems.stream()
                                   .mapToDouble(BricklinkSaleItem::getUnitPrice)
                                   .toArray();
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
        if ((prices[prices.length - 1] / prices[prices.length - 2]) > 3) {
            throw new PriceNotCalculableException(bricklinkInventory, "[" + bricklinkInventory.getNewOrUsed() + " " + bricklinkInventory.getCompleteness() + " " + bricklinkInventory.getBlItemNo() + " " + bricklinkInventory.getBlItemId() + "] Delta between last two prices is too high [" + prices[prices.length - 1] + "], [" + prices[prices.length - 2] + "]");
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
