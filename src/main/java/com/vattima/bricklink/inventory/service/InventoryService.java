package com.vattima.bricklink.inventory.service;

import com.bricklink.api.rest.model.v1.Order;
import com.vattima.bricklink.inventory.support.SynchronizeResult;
import net.bricklink.data.lego.dto.BricklinkInventory;

import java.util.function.Supplier;

public interface InventoryService {
    SynchronizeResult synchronize(BricklinkInventory bricklinkInventory);
    void updateInventoryItemsOnOrder(final String orderId);
}
