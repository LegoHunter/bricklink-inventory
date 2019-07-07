package com.vattima.bricklink.inventory.service;

import com.bricklink.api.ajax.BricklinkAjaxClient;
import com.bricklink.api.rest.client.BricklinkRestClient;
import com.bricklink.api.rest.model.v1.BricklinkResource;
import com.bricklink.api.rest.model.v1.Inventory;
import com.vattima.bricklink.inventory.data.mapper.InventoryMapper;
import com.vattima.bricklink.inventory.support.SynchronizeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.dao.BricklinkInventoryDao;
import net.bricklink.data.lego.dto.BricklinkInventory;
import org.springframework.stereotype.Component;

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
        log.info("Synchronizing Bricklink Inventory [{}]", bricklinkInventory);
        if (bricklinkInventory.shouldSynchronize()) {
            Optional<Long> blInventoryId = Optional.ofNullable(bricklinkInventory.getInventoryId());
            if (blInventoryId.isPresent()) {
                // If inventory_id is not null, bricklinkInventory must be updated in Bricklink
                BricklinkResource<Inventory> inventoryResponse = bricklinkRestClient.getInventories(blInventoryId.get());
                result = SynchronizeResult.build(inventoryResponse);
                if (result.isSuccess()) {
                    Inventory inventory = result.getInventory();
                    InventoryMapper.mapBricklinkInventoryToInventory.accept(bricklinkInventory, inventory);
                    bricklinkRestClient.updateInventory(blInventoryId.get(), inventory);
                    bricklinkInventoryDao.setSynchronizedNow(bricklinkInventory.getBlInventoryId());
                }
            } else {
                // If inventory_id is null, bricklinkInventory does not exist in Bricklink
                Inventory inventory = new Inventory();
                InventoryMapper.mapBricklinkInventoryToInventory.accept(bricklinkInventory, inventory);
                BricklinkResource<Inventory> inventoryResponse = bricklinkRestClient.createInventory(inventory);
                result = SynchronizeResult.build(inventoryResponse);
                if (result.isSuccess()) {
                    inventory = result.getInventory();
                    InventoryMapper.mapInventoryToBricklinkInventory.accept(inventory, bricklinkInventory);
                    bricklinkInventoryDao.update(bricklinkInventory);
                    bricklinkInventoryDao.setSynchronizedNow(bricklinkInventory.getBlInventoryId());
                }
            }
        } else {
            log.info("Synchronization not needed");
        }
        return result;
    }
}
