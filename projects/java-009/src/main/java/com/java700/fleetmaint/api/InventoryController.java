package com.java700.fleetmaint.api;

import com.java700.fleetmaint.security.SecurityUtil;
import com.java700.fleetmaint.service.Api;
import com.java700.fleetmaint.service.InventoryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Parts inventory, restocking and the reorder report. */
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventory;

    public InventoryController(InventoryService inventory) {
        this.inventory = inventory;
    }

    @GetMapping("/parts")
    @PreAuthorize("hasAnyRole('PARTS_CLERK','FLEET_MANAGER','MECHANIC','AUDITOR','ADMIN')")
    public List<Api.PartView> parts() {
        return inventory.all().stream().map(inventory::view).toList();
    }

    @GetMapping("/parts/reorder")
    @PreAuthorize("hasAnyRole('PARTS_CLERK','FLEET_MANAGER','ADMIN')")
    public List<Api.PartView> reorderReport() {
        return inventory.reorderReport().stream().map(inventory::view).toList();
    }

    @PostMapping("/parts/{partCode}/restock")
    @PreAuthorize("hasAnyRole('PARTS_CLERK','ADMIN')")
    public Api.PartView restock(@PathVariable String partCode,
                                @Valid @RequestBody Api.RestockRequest request) {
        return inventory.view(inventory.restock(partCode, request.quantity(),
                SecurityUtil.currentUsername()));
    }
}
