package com.java700.fleetmaint.service;

import com.java700.fleetmaint.common.api.Problems;
import com.java700.fleetmaint.common.audit.AuditLogService;
import com.java700.fleetmaint.domain.Part;
import com.java700.fleetmaint.domain.PartRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Parts inventory: stock levels, restocking and reorder reporting. */
@Service
public class InventoryService {

    private final PartRepository parts;
    private final AuditLogService audit;

    public InventoryService(PartRepository parts, AuditLogService audit) {
        this.parts = parts;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<Part> all() {
        return parts.findAll();
    }

    @Transactional
    public Part restock(String partCode, int quantity, String username) {
        if (quantity <= 0) {
            throw new Problems.BadRequest("restock quantity must be positive");
        }
        Part part = parts.findByPartCode(partCode)
                .orElseThrow(() -> new Problems.NotFound("part " + partCode));
        part.restock(quantity);
        Part saved = parts.save(part);
        audit.record("PART_RESTOCKED", "part", saved.getPartCode(),
                "qty=" + quantity + " onHand=" + saved.getQuantityOnHand() + " by=" + username);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Part> reorderReport() {
        return parts.findAll().stream()
                .filter(part -> part.getQuantityOnHand() - part.getReservedQty()
                        <= part.getReorderPoint())
                .sorted(Comparator.comparing(Part::getPartCode))
                .toList();
    }

    public Api.PartView view(Part part) {
        int available = part.getQuantityOnHand() - part.getReservedQty();
        return new Api.PartView(part.getId(), part.getPartCode(), part.getName(),
                part.getQuantityOnHand(), part.getReservedQty(), part.getReorderPoint(),
                part.getUnitCost(), available <= part.getReorderPoint());
    }
}
