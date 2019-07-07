package com.vattima.bricklink.inventory.support;

import com.bricklink.api.rest.model.v1.BricklinkMeta;
import com.bricklink.api.rest.model.v1.BricklinkResource;
import com.bricklink.api.rest.model.v1.Inventory;
import lombok.Data;

@Data
public class SynchronizeResult {
    private boolean success;
    private int code;
    private String message;
    private String description;

    private Inventory inventory;

    public static SynchronizeResult build(BricklinkResource<Inventory> inventoryResponse) {
        SynchronizeResult result = new SynchronizeResult();
        BricklinkMeta bricklinkMeta = inventoryResponse.getMeta();
        result.setCode(bricklinkMeta.getCode());
        if (result.getCode() == 500) {
            result.setSuccess(false);
            result.setMessage(bricklinkMeta.getMessage());
            result.setDescription(bricklinkMeta.getDescription());
        } else if (result.getCode() >= 200 && result.getCode() <= 299) {
            result.setSuccess(true);
            result.setInventory(inventoryResponse.getData());
        }
        return result;
    }
}
