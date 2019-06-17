package com.vattima.lego.inventory.pricing;

import lombok.Getter;
import lombok.ToString;
import net.bricklink.data.lego.dto.BricklinkInventory;

@Getter
@ToString
public class PriceNotCalculableException extends RuntimeException {
    private BricklinkInventory bricklinkInventory;

    public PriceNotCalculableException(final BricklinkInventory bricklinkInventory) {
        super();
        this.bricklinkInventory = bricklinkInventory;
    }

    public PriceNotCalculableException(final BricklinkInventory bricklinkInventory, String message) {
        super(message);
        this.bricklinkInventory = bricklinkInventory;
    }

    public PriceNotCalculableException(final BricklinkInventory bricklinkInventory, String message, Throwable cause) {
        super(message, cause);
        this.bricklinkInventory = bricklinkInventory;
    }

    public PriceNotCalculableException(final BricklinkInventory bricklinkInventory, Throwable cause) {
        super(cause);
        this.bricklinkInventory = bricklinkInventory;
    }

    protected PriceNotCalculableException(final BricklinkInventory bricklinkInventory, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.bricklinkInventory = bricklinkInventory;
    }
}
