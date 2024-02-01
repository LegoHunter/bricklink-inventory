package com.vattima.bricklink.inventory.service;

import com.bricklink.api.rest.client.BricklinkRestClient;
import com.bricklink.api.rest.model.v1.BricklinkResource;
import com.bricklink.api.rest.model.v1.Inventory;
import com.bricklink.api.rest.model.v1.Order;
import com.bricklink.api.rest.model.v1.OrderItem;
import com.vattima.bricklink.inventory.BricklinkInventoryException;
import com.vattima.bricklink.inventory.data.mapper.InventoryMapper;
import com.vattima.bricklink.inventory.support.SynchronizeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.dao.BricklinkInventoryDao;
import net.bricklink.data.lego.dto.BricklinkInventory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final BricklinkInventoryDao bricklinkInventoryDao;
    private final BricklinkRestClient bricklinkRestClient;

    @Override
    public SynchronizeResult synchronize(BricklinkInventory bricklinkInventory) {
        SynchronizeResult result = null;
        if (bricklinkInventory.shouldSynchronize()) {
            log.info("Synchronizing Bricklink Inventory [{}]", bricklinkInventory);
            Optional<Long> blInventoryId = Optional.ofNullable(bricklinkInventory.getInventoryId());
            if (blInventoryId.isPresent()) {
                // If inventory_id is not null, bricklinkInventory must be updated in Bricklink
                BricklinkResource<Inventory> inventoryResponse = bricklinkRestClient.getInventories(blInventoryId.get());
                result = SynchronizeResult.build(inventoryResponse);
                if (result.isSuccess()) {
                    Inventory inventory = result.getInventory();
                    bricklinkInventoryDao.update(bricklinkInventory);
                    InventoryMapper.mapBricklinkInventoryToInventory.accept(bricklinkInventory, inventory);
                    bricklinkRestClient.updateInventory(blInventoryId.get(), inventory);
                    bricklinkInventoryDao.setSynchronizedNow(bricklinkInventory.getBlInventoryId());
                }
            } else {
                // If inventory_id is null, bricklinkInventory does not exist in Bricklink
                throw new RuntimeException(String.format("ABOUT TO INSERT NEW INVENTORY [%s]", bricklinkInventory));
//                Inventory inventory = new Inventory();
//                InventoryMapper.mapBricklinkInventoryToInventory.accept(bricklinkInventory, inventory);
//                BricklinkResource<Inventory> inventoryResponse = bricklinkRestClient.createInventory(inventory);
//                result = SynchronizeResult.build(inventoryResponse);
//                if (result.isSuccess()) {
//                    inventory = result.getInventory();
//                    InventoryMapper.mapInventoryToBricklinkInventory.accept(inventory, bricklinkInventory);
//                    bricklinkInventoryDao.update(bricklinkInventory);
//                    bricklinkInventoryDao.setSynchronizedNow(bricklinkInventory.getBlInventoryId());
//                }
            }
        } else {
            log.info("Synchronization not needed");
        }
        return result;
    }

    @Override
    public void updateInventoryItemsOnOrder(final String orderId) {
        // Retrieve order from Bricklink
        BricklinkResource<Order> bricklinkOrder = bricklinkRestClient.getOrder(orderId);

        // If not found, throw Exception
        Order order = Optional.ofNullable(bricklinkOrder.getData()).orElseThrow(() -> new BricklinkInventoryException("Order Id [%1$s] was not found".formatted(orderId)));

        // Get all OrderItem Batches
        BricklinkResource<List<List<OrderItem>>> orderItemBatches = bricklinkRestClient.getOrderItems(orderId);

        // For each OrderItemBatch
        orderItemBatches.getData().forEach(oib -> {
            // For each OrderItem
            oib.forEach(oi -> {
                // find bricklink inventory by inventory id
                Optional<BricklinkInventory> bricklinkInventory = null;
                try {
                    bricklinkInventory = bricklinkInventoryDao.getByInventoryId(oi.getInventory_id());
                    if (bricklinkInventory.isEmpty()) {
                        throw new IllegalStateException(String.format("Bricklink Inventory Id [%d] was not found.", oi.getInventory_id()));
                    }
                    bricklinkInventory.ifPresent(bi -> {
                        // update database Bricklink Inventory item with the order id
                        bricklinkInventoryDao.updateOrder(bi.getBlInventoryId(), orderId);
                        // log the update
                        log.info("Updated BricklinkInventory [{}] with Order Id [{}]", bi, orderId);
                    });
                } catch (Exception e) {
                    throw new BricklinkInventoryException(e);
                }
            });
        });
    }
}
