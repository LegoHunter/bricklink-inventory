package com.vattima.bricklink.inventory.support;

import net.bricklink.data.lego.dto.BricklinkInventory;

import java.util.function.Function;

public interface SynchronizeFunction extends Function<BricklinkInventory, SynchronizeResult> {

}
