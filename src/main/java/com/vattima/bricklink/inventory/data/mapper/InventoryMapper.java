package com.vattima.bricklink.inventory.data.mapper;

import com.bricklink.api.rest.model.v1.Inventory;
import com.bricklink.api.rest.model.v1.Item;
import net.bricklink.data.lego.dto.BricklinkInventory;

import java.time.Instant;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class InventoryMapper {
    private InventoryMapper() {
    }

    public static BiConsumer<BricklinkInventory, Inventory> mapBricklinkInventoryToInventory = (bricklinkInventory, inventory) -> {
        inventory.setInventory_id(bricklinkInventory.getInventoryId());
        Item item = new Item();
        item.setNo(bricklinkInventory.getBlItemNo());
        item.setType(bricklinkInventory.getItemType());
        inventory.setItem(item);
        inventory.setColor_id(Optional.ofNullable(bricklinkInventory.getColorId()).orElse(0));
        inventory.setColor_name(bricklinkInventory.getColorName());
        inventory.setQuantity(bricklinkInventory.getQuantity() - inventory.getQuantity());
        inventory.setNew_or_used(bricklinkInventory.getNewOrUsed());
        inventory.setCompleteness(bricklinkInventory.getCompleteness());
        inventory.setUnit_price(bricklinkInventory.getUnitPrice());
        inventory.setBind_id(bricklinkInventory.getBindId());
        inventory.setDescription(bricklinkInventory.getDescription());
        inventory.setRemarks(bricklinkInventory.getUuid() + Optional.ofNullable(bricklinkInventory.getInternalComments()).map(ic -> "; " + ic).orElse(""));
        inventory.setBulk(bricklinkInventory.getBulk());
        inventory.setIs_retain(bricklinkInventory.getIsRetain());
        inventory.setIs_stock_room(bricklinkInventory.getIsStockRoom());
        inventory.setStock_room_id(bricklinkInventory.getStockRoomId());
        inventory.setMy_cost(bricklinkInventory.getMyCost());
        inventory.setSale_rate(bricklinkInventory.getSaleRate());
        inventory.setTier_quantity1(bricklinkInventory.getTierQuantity1());
        inventory.setTier_price1(bricklinkInventory.getTierPrice1());
        inventory.setTier_quantity2(bricklinkInventory.getTierQuantity2());
        inventory.setTier_price2(bricklinkInventory.getTierPrice2());
        inventory.setTier_quantity3(bricklinkInventory.getTierQuantity3());
        inventory.setTier_price3(bricklinkInventory.getTierPrice3());
        inventory.setMy_weight(bricklinkInventory.getMyWeight());
        inventory.setDate_created(null); // This should never be sent over the wire to Bricklink
    };

    public static BiConsumer<Inventory, BricklinkInventory> mapInventoryToBricklinkInventory = (inventory, bricklinkInventory) -> {
        bricklinkInventory.setInventoryId(inventory.getInventory_id());
        bricklinkInventory.setColorId(Optional.ofNullable(inventory.getColor_id()).orElse(0));
        bricklinkInventory.setColorName(inventory.getColor_name());
        bricklinkInventory.setQuantity(inventory.getQuantity());
        bricklinkInventory.setNewOrUsed(inventory.getNew_or_used());
        bricklinkInventory.setCompleteness(inventory.getCompleteness());
        bricklinkInventory.setUnitPrice(inventory.getUnit_price());
        bricklinkInventory.setBindId(inventory.getBind_id());
        bricklinkInventory.setDescription(inventory.getDescription());
        bricklinkInventory.setRemarks(inventory.getRemarks());
        bricklinkInventory.setBulk(inventory.getBulk());
        bricklinkInventory.setIsRetain(inventory.getIs_retain());
        bricklinkInventory.setIsStockRoom(inventory.getIs_stock_room());
        bricklinkInventory.setStockRoomId(inventory.getStock_room_id());
        bricklinkInventory.setMyCost(inventory.getMy_cost());
        bricklinkInventory.setSaleRate(inventory.getSale_rate());
        bricklinkInventory.setTierQuantity1(inventory.getTier_quantity1());
        bricklinkInventory.setTierPrice1(inventory.getTier_price1());
        bricklinkInventory.setTierQuantity2(inventory.getTier_quantity2());
        bricklinkInventory.setTierPrice2(inventory.getTier_price2());
        bricklinkInventory.setTierQuantity3(inventory.getTier_quantity3());
        bricklinkInventory.setTierPrice3(inventory.getTier_price3());
        bricklinkInventory.setMyWeight(inventory.getMy_weight());
        bricklinkInventory.setDateCreated(Optional.ofNullable(inventory.getDate_created()).orElseGet(bricklinkInventory::getDateCreated));
    };
}
