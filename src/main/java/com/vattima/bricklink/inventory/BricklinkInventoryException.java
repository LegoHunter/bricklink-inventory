package com.vattima.bricklink.inventory;

public class BricklinkInventoryException extends RuntimeException {
    public BricklinkInventoryException() {
        super();
    }

    public BricklinkInventoryException(String message) {
        super(message);
    }

    public BricklinkInventoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public BricklinkInventoryException(Throwable cause) {
        super(cause);
    }

    protected BricklinkInventoryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
