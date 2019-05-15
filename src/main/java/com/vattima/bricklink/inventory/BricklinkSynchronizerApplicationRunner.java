package com.vattima.bricklink.inventory;

import com.vattima.bricklink.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bricklink.data.lego.dao.BricklinkInventoryDao;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

//@Component
@Slf4j
@RequiredArgsConstructor
public class BricklinkSynchronizerApplicationRunner implements ApplicationRunner {

    private final BricklinkInventoryDao bricklinkInventoryDao;
    private final InventoryService inventoryService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Starting [{}]", this.getClass().getCanonicalName());

        //bricklinkInventoryDao.getInventoryWork().forEach(System.out::println);

        inventoryService.synchronize(bricklinkInventoryDao.get(1));

        log.info("Completed [{}]", this.getClass().getCanonicalName());
    }
}
