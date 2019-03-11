package com.vattima.bricklink.inventory.service;

import com.vattima.bricklink.inventory.support.SynchronizeResult;
import net.bricklink.data.lego.dto.BricklinkInventory;

import java.util.function.Supplier;

public interface InventoryService {
    SynchronizeResult synchronize(BricklinkInventory bricklinkInventory);
}
