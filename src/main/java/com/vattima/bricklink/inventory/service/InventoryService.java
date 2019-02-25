package com.vattima.bricklink.inventory.service;

import net.bricklink.data.lego.dto.BricklinkInventory;

import java.util.function.Supplier;

public interface InventoryService {
    void synchronize(Supplier<BricklinkInventory> bricklinkInventory);
}
