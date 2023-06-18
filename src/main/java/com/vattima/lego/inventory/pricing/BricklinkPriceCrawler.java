package com.vattima.lego.inventory.pricing;

import com.bricklink.api.ajax.BricklinkAjaxClient;
import com.bricklink.api.ajax.model.v1.ItemForSale;
import com.bricklink.api.ajax.support.CatalogItemsForSaleResult;
import com.bricklink.api.rest.client.BricklinkRestClient;
import com.bricklink.api.rest.client.ParamsBuilder;
import com.bricklink.api.rest.model.v1.PriceGuide;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.dao.BricklinkInventoryDao;
import net.bricklink.data.lego.dao.BricklinkSaleItemDao;
import net.bricklink.data.lego.dto.BricklinkInventory;
import net.bricklink.data.lego.dto.BricklinkSaleItem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
@RequiredArgsConstructor
public class BricklinkPriceCrawler {
    private static final Integer ONE = 1;

    private final BricklinkInventoryDao bricklinkInventoryDao;
    private final BricklinkRestClient bricklinkRestClient;
    private final BricklinkAjaxClient bricklinkAjaxClient;
    private final BricklinkSaleItemDao bricklinkSaleItemDao;

    private Function<BricklinkInventory, Stream<InventoryWorkHolder>> inventoryWorkHolders = iwh -> Stream.of(
            new InventoryWorkHolder(iwh.getItemType(), "stock", "N", iwh),
            new InventoryWorkHolder(iwh.getItemType(), "sold", "N", iwh),
            new InventoryWorkHolder(iwh.getItemType(), "stock", "U", iwh),
            new InventoryWorkHolder(iwh.getItemType(), "sold", "U", iwh)
    );

    public void crawlPrices() {
        logInventoryItems(updateBricklinkSaleItems(inventoryItems()));
    }

    private Stream<BricklinkInventory> inventoryItems() {
        return bricklinkInventoryDao.getInventoryWork()
                                    .parallelStream();
                                    //.filter(BricklinkInventory::shouldSynchronize);
    }

//    private void updateBricklinkSaleItems2(Stream<BricklinkInventory> bricklinkInventoryStream) {
//        bricklinkInventoryStream.flatMap(bli -> inventoryWorkHolders.apply(bli))
//                                .parallel()
//                                .flatMap(iwh -> bricklinkAjaxClient.catalogItemsForSale(
//                                        new ParamsBuilder()
//                                                .of("itemid", iwh
//                                                        .getBricklinkInventory()
//                                                        .getBlItemId())
//                                                .of("cond", iwh.getNewUsed())
//                                                .of("rpp", 500)
//                                                .get())
//                                                                   .getList()
//                                                                   .stream()
//                                                                   .map(ifs -> buildBricklinkSaleItem(iwh.getBricklinkInventory()
//                                                                                                         .getBlItemId(), ifs)))
//                                .peek(bsi -> {
//                                    try {
//                                        bricklinkSaleItemDao.upsert(bsi);
//                                    } catch (Exception e) {
//                                        log.error("Could not upsert [" + bsi + "]", e);
//                                        e.printStackTrace();
//                                    }
//                                })
//                                .collect(Collector.of((Supplier<ConcurrentHashMap<Long, CopyOnWriteArrayList<BricklinkSaleItem>>>) ConcurrentHashMap::new,
//                                        (m, bsi) -> {
//                                            if (m.containsKey(bsi.getBlItemId())) {
//                                                m.get(bsi.getBlItemId()).add(bsi);
//                                            } else {
//                                                CopyOnWriteArrayList<BricklinkSaleItem> list = new CopyOnWriteArrayList<>();
//                                                list.add(bsi);
//                                                m.put(bsi.getBlItemId(), list);
//                                            }
//                                        },
//                                        (m1, m2) -> {
//                                            m1.putAll(m2);
//                                            return m1;
//                                        })).entrySet().stream().flatMap(es -> {
//            bricklinkSaleItemDao.updateBricklinkSaleItemSold(es.getKey(), iwh.getNewUsed(), iwh.getCurrentlyForSaleInventoryIds());
//        });
//    }

    private List<InventoryWorkHolder> updateBricklinkSaleItems(Stream<BricklinkInventory> bricklinkInventoryStream) {
        return bricklinkInventoryStream.filter(bli -> Optional.ofNullable(bli.getOrderId()).isEmpty())
                                       .map(bli -> inventoryWorkHolders.apply(bli))
                                       .flatMap(s -> s.peek(iwh -> {
                                                   if (iwh.getGuideType()
                                                          .equals("stock")) {
                                                       synchronized (this) {
                                                           try {
                                                               Thread.sleep(1300);
                                                               CatalogItemsForSaleResult catalogNewItemsForSaleResult = bricklinkAjaxClient.catalogItemsForSale(
                                                                       new ParamsBuilder()
                                                                               .of("itemid", iwh
                                                                                       .getBricklinkInventory()
                                                                                       .getBlItemId())
                                                                               .of("cond", iwh.getNewUsed())
                                                                               .of("rpp", 500)
                                                                               .get());
                                                               iwh.setItemsForSale(catalogNewItemsForSaleResult.getList());
                                                               iwh.getItemsForSale()
                                                                  .forEach(ifs -> {
                                                                      BricklinkSaleItem bricklinkSaleItem = iwh.buildBricklinkSaleItem(ifs);
                                                                      try {
                                                                          bricklinkSaleItemDao.upsert(bricklinkSaleItem);
                                                                      } catch (Exception e) {
                                                                          log.error("Could not upsert [" + bricklinkSaleItem + "]", e);
                                                                      }
                                                                  });
                                                               bricklinkSaleItemDao.updateBricklinkSaleItemSold(iwh.getBricklinkInventory()
                                                                                                                   .getBlItemId(), iwh.getNewUsed(), iwh.getCurrentlyForSaleInventoryIds());
                                                               if (iwh.getCurrentlyForSaleInventoryIds()
                                                                      .size() == 0) {
                                                                   log.info("No items currently for sale for item [{}] new/Used [{}]", iwh.getBricklinkInventory()
                                                                                                                                          .getBlItemNo(), iwh.getNewUsed());
                                                               }
                                                           } catch (InterruptedException e) {
                                                               e.printStackTrace();
                                                           }
                                                       }
                                                   }
                                                   PriceGuide pg = bricklinkRestClient.getPriceGuide(iwh.getType(),
                                                           iwh.getBricklinkInventory()
                                                              .getBlItemNo(),
                                                           new ParamsBuilder()
                                                                   .of("type", iwh.getType())
                                                                   .of("guide_type", iwh.getGuideType())
                                                                   .of("new_or_used", iwh.getNewUsed())
                                                                   .get())
                                                                                      .getData();

                                                   iwh.setPriceGuide(pg);
                                               })
                                       )
                                       .collect(Collectors.toList());
    }

    private void logInventoryItems(List<InventoryWorkHolder> inventoryWorkHolders) {
        inventoryWorkHolders.parallelStream()
                            .peek(iwh -> {
                                PriceGuide pg = iwh.getPriceGuide();
                                log.info("[{}::#{} Stock/Sold:{} New/Used: {} min:{} avg:{} max:{}]",
                                        iwh.getBricklinkInventory()
                                           .getBlItemId(),
                                        pg.getItem()
                                          .getNo(),
                                        iwh.getGuideType(),
                                        pg.getNew_or_used(),
                                        pg.getMin_price(),
                                        pg.getAvg_price(),
                                        pg.getMax_price());
                            })
                            .collect(Collectors.toList());
    }

    @Setter
    @Getter
    @RequiredArgsConstructor
    @ToString
    private class InventoryWorkHolder {
        private final String type;
        private final String guideType;
        private final String newUsed;
        private final BricklinkInventory bricklinkInventory;
        private PriceGuide priceGuide = new PriceGuide();
        private List<ItemForSale> itemsForSale = new ArrayList<>();

        public void setItemsForSale(List<ItemForSale> itemsForSale) {
            this.itemsForSale = Optional.ofNullable(itemsForSale).orElseThrow(() -> new RuntimeException("itemsForSale cannot be null - BricklinkInventory [%s]".formatted(bricklinkInventory)));
        }

        BricklinkSaleItem buildBricklinkSaleItem(ItemForSale itemForSale) {
            BricklinkSaleItem bricklinkSaleItem = new BricklinkSaleItem();
            bricklinkSaleItem.setBlItemId(getBricklinkInventory().getBlItemId());
            bricklinkSaleItem.setInventoryId(itemForSale.getIdInv());
            bricklinkSaleItem.setCompleteness(itemForSale.getCodeComplete());
            bricklinkSaleItem.setDateCreated(ZonedDateTime.now());
            bricklinkSaleItem.setDescription(StringUtils.trim(Optional.ofNullable(itemForSale.getStrDesc())
                                                                      .map(d -> d.replaceAll("[^\\x00-\\x7F]", ""))
                                                                      .orElse("")));
            bricklinkSaleItem.setHasExtendedDescription(ONE.equals(itemForSale.getHasExtendedDescription()));
            bricklinkSaleItem.setNewOrUsed(itemForSale.getCodeNew());
            bricklinkSaleItem.setQuantity(itemForSale.getN4Qty());
            bricklinkSaleItem.setUnitPrice(itemForSale.getSalePrice());
            bricklinkSaleItem.setCountryCode((itemForSale.getStrSellerCountryCode()));
            return bricklinkSaleItem;
        }

        public List<Integer> getCurrentlyForSaleInventoryIds() {
            return itemsForSale.stream()
                               .map(ItemForSale::getIdInv)
                               .collect(Collectors.toList());
        }
    }

    private BricklinkSaleItem buildBricklinkSaleItem(Long blItemid, ItemForSale itemForSale) {
        BricklinkSaleItem bricklinkSaleItem = new BricklinkSaleItem();
        bricklinkSaleItem.setBlItemId(blItemid);
        bricklinkSaleItem.setInventoryId(itemForSale.getIdInv());
        bricklinkSaleItem.setCompleteness(itemForSale.getCodeComplete());
        bricklinkSaleItem.setDateCreated(ZonedDateTime.now());
        bricklinkSaleItem.setDescription(StringUtils.trim(Optional.ofNullable(itemForSale.getStrDesc())
                                                                  .map(d -> d.replaceAll("[^\\x00-\\x7F]", ""))
                                                                  .orElse("")));
        bricklinkSaleItem.setHasExtendedDescription(ONE.equals(itemForSale.getHasExtendedDescription()));
        bricklinkSaleItem.setNewOrUsed(itemForSale.getCodeNew());
        bricklinkSaleItem.setQuantity(itemForSale.getN4Qty());
        bricklinkSaleItem.setUnitPrice(itemForSale.getSalePrice());
        return bricklinkSaleItem;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {

        }
    }
}
