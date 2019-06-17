package com.vattima.bricklink.inventory.service;

import com.bricklink.api.rest.client.BricklinkRestClient;
import com.vattima.lego.inventory.pricing.PriceNotCalculableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.dao.BricklinkInventoryDao;
import net.bricklink.data.lego.dao.BricklinkSaleItemDao;
import net.bricklink.data.lego.dto.BricklinkInventory;
import net.bricklink.data.lego.dto.BricklinkSaleItem;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;

@Slf4j
@RequiredArgsConstructor
@Component
public class PriceCalculatorService {
    private final BricklinkSaleItemDao bricklinkSaleItemDao;
    private final BricklinkInventoryDao bricklinkInventoryDao;
    private final BricklinkRestClient bricklinkRestClient;

    public double calculatePrice(final BricklinkInventory bricklinkInventory) {
        double price = 0.0d;
        double newPrice = 0.0d;
        double usedPrice = 0.0d;
        double[] newPrices = getNewPrices(bricklinkInventory.getBlItemId(), bricklinkInventory.getCompleteness());
        double[] usedPrices = getUsedPrices(bricklinkInventory.getBlItemId(), bricklinkInventory.getCompleteness());

        int newPricesCount = newPrices.length;
        int usedPricesCount = usedPrices.length;
        if ("N".equals(bricklinkInventory.getNewOrUsed())) {
            if (newPricesCount > 2) {
                newPrice = calculatePrice(bricklinkInventory, newPrices);
            } else if (newPricesCount > 1) {
                newPrice = calculatePriceUsingTwoPrices(bricklinkInventory, newPrices);
            } else if (newPricesCount > 0) {
                newPrice = calculatePriceUsingOnePrice(bricklinkInventory, newPrices);
            } else {
                throw new PriceNotCalculableException(bricklinkInventory, "There are no prices with which to calculate a price");
            }
            price = newPrice;
        } else {
            if (usedPricesCount > 2) {
                usedPrice = calculatePrice(bricklinkInventory, usedPrices);
            } else if (usedPricesCount > 1) {
                usedPrice = calculatePriceUsingTwoPrices(bricklinkInventory, usedPrices);
            } else if (usedPricesCount > 0) {
                usedPrice = calculatePriceUsingOnePrice(bricklinkInventory, usedPrices);
            } else {
                throw new PriceNotCalculableException(bricklinkInventory, "There are no prices with which to calculate a price");
            }
            price = usedPrice;
        }
        return price;
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

    public double calculatePriceUsingTwoPrices(final BricklinkInventory bricklinkInventory, double[] prices) {
        double lowPrice = prices[0];
        double highPrice = prices[1];
        return lowPrice + (highPrice - lowPrice) * 0.75d;
    }

    public double calculatePriceUsingOnePrice(final BricklinkInventory bricklinkInventory, double[] prices) {
        double price = prices[0];
        return price * 0.85d;
    }

    public double calculatePrice(final BricklinkInventory bricklinkInventory, double[] prices) {
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
        if ((prices[prices.length - 1] / prices[prices.length - 2]) > 3) {
            throw new PriceNotCalculableException(bricklinkInventory, "["+bricklinkInventory.getNewOrUsed()+" "+bricklinkInventory.getCompleteness()+" "+bricklinkInventory.getBlItemNo()+" "+bricklinkInventory.getBlItemId()+"] Delta between last two prices is too high [" + prices[prices.length - 1] + "], [" + prices[prices.length - 2] + "]");
        }
        Arrays.stream(prices)
              .forEach(p -> {
                  descriptiveStatistics.addValue(p);
              });
        double mean = descriptiveStatistics.getMean();
        double standardDeviation = descriptiveStatistics.getStandardDeviation();
        return mean + standardDeviation;
    }
}
