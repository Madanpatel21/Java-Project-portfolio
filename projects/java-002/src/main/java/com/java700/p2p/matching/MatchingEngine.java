package com.java700.p2p.matching;

import com.java700.p2p.domain.GoodsReceipt;
import com.java700.p2p.domain.GoodsReceiptLine;
import com.java700.p2p.domain.Invoice;
import com.java700.p2p.domain.InvoiceLine;
import com.java700.p2p.domain.MatchException.Type;
import com.java700.p2p.domain.PurchaseOrder;
import com.java700.p2p.domain.PurchaseOrderLine;
import com.java700.p2p.domain.ToleranceRule;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Three-way matching engine: PO ↔ goods receipt ↔ invoice.
 *
 * <p>Pure, deterministic, unit-testable. For every invoice line it finds the PO line
 * (supplier + normalized item code), compares invoice quantity against the received quantity
 * and the invoice unit price against the PO unit price, applying active tolerance rules.
 * Missing receipts, missing PO matches and quantity over-billing are CRITICAL; variance
 * beyond tolerance is CRITICAL; variance inside tolerance is WARNING.</p>
 */
public final class MatchingEngine {

    public static final double SUPPLIER_FUZZY_THRESHOLD = 0.92;

    private MatchingEngine() {
    }

    /**
     * @param invoice the invoice under evaluation
     * @param lines its lines
     * @param pos all purchase orders of the supplier
     * @param poLinesByPo PO lines keyed by PO id
     * @param receiptsByPo goods receipts keyed by PO id
     * @param receiptLinesByGr receipt lines keyed by GR id
     * @param rules active tolerance rules
     */
    public static MatchResult match(Invoice invoice, List<InvoiceLine> lines,
                                    List<PurchaseOrder> pos,
                                    Map<String, List<PurchaseOrderLine>> poLinesByPo,
                                    Map<String, List<GoodsReceipt>> receiptsByPo,
                                    Map<String, List<GoodsReceiptLine>> receiptLinesByGr,
                                    List<ToleranceRule> rules) {
        Map<ToleranceRule.RuleType, ToleranceRule> ruleMap = new LinkedHashMap<>();
        for (ToleranceRule r : rules) {
            ruleMap.put(r.getRuleType(), r);
        }
        List<Finding> findings = new ArrayList<>();

        // supplier fuzzy check
        Optional<PurchaseOrder> supplierMatch = pos.stream()
                .filter(po -> FuzzyNormalizer.supplier(po.getSupplierName())
                        .equals(FuzzyNormalizer.supplier(invoice.getSupplierName()))
                        || FuzzyNormalizer.jaroWinkler(
                                FuzzyNormalizer.supplier(po.getSupplierName()),
                                FuzzyNormalizer.supplier(invoice.getSupplierName()))
                                >= SUPPLIER_FUZZY_THRESHOLD)
                .findFirst();
        if (supplierMatch.isEmpty()) {
            findings.add(new Finding(Type.NO_PO_MATCH, Severity.CRITICAL,
                    "No purchase order found for supplier " + invoice.getSupplierName()));
            return new MatchResult(MatchStatus.EXCEPTION, findings);
        }
        String currency = supplierMatch.get().getCurrency();
        if (!currency.equals(invoice.getCurrency())) {
            findings.add(new Finding(Type.NO_PO_MATCH, Severity.CRITICAL,
                    "Invoice currency " + invoice.getCurrency() + " does not match PO currency "
                            + currency));
            return new MatchResult(MatchStatus.EXCEPTION, findings);
        }

        // aggregate received quantities per PO line across all receipts
        Map<String, BigDecimal> receivedByPoLine = new LinkedHashMap<>();
        for (GoodsReceipt gr : receiptsByPo.values().stream().flatMap(List::stream).toList()) {
            for (GoodsReceiptLine grl : receiptLinesByGr.getOrDefault(gr.getId(), List.of())) {
                receivedByPoLine.merge(grl.getPoLineId(), grl.getQuantityReceived(),
                        BigDecimal::add);
            }
        }
        boolean anyReceipt = receivedByPoLine.values().stream()
                .anyMatch(q -> q.signum() > 0);

        if (!anyReceipt) {
            findings.add(new Finding(Type.MISSING_RECEIPT, Severity.CRITICAL,
                    "Invoice " + invoice.getInvoiceNumber()
                            + " has no matching goods receipt (three-way match fails)"));
        }

        // line matching
        Map<String, PurchaseOrderLine> poLineByItem = new LinkedHashMap<>();
        for (PurchaseOrder po : pos) {
            for (PurchaseOrderLine pol : poLinesByPo.getOrDefault(po.getId(), List.of())) {
                poLineByItem.putIfAbsent(FuzzyNormalizer.itemCode(pol.getItemCode()), pol);
            }
        }
        for (InvoiceLine line : lines) {
            PurchaseOrderLine pol = poLineByItem.get(FuzzyNormalizer.itemCode(line.getItemCode()));
            if (pol == null) {
                findings.add(new Finding(Type.NO_PO_MATCH, Severity.CRITICAL,
                        "Invoice line item " + line.getItemCode()
                                + " does not match any PO line"));
                continue;
            }
            BigDecimal received = receivedByPoLine.getOrDefault(pol.getId(), BigDecimal.ZERO);
            BigDecimal invoiced = line.getQuantity();

            // price variance
            BigDecimal poPrice = pol.getUnitPrice();
            BigDecimal invoicePrice = line.getUnitPrice();
            if (poPrice.signum() > 0) {
                BigDecimal variancePct = invoicePrice.subtract(poPrice).abs()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(poPrice, 3, RoundingMode.HALF_UP);
                ToleranceRule priceRule = ruleMap.get(ToleranceRule.RuleType.PRICE_VARIANCE);
                if (priceRule != null && priceRule.isActive()
                        && variancePct.compareTo(priceRule.getTolerancePct()) > 0) {
                    findings.add(new Finding(Type.PRICE_VARIANCE, Severity.CRITICAL,
                            "Item " + line.getItemCode() + " unit price " + invoicePrice
                                    + " exceeds PO price " + poPrice + " by " + variancePct + "%"));
                } else if (variancePct.signum() > 0) {
                    findings.add(new Finding(Type.PRICE_VARIANCE, Severity.WARNING,
                            "Item " + line.getItemCode() + " price variance " + variancePct
                                    + "% within tolerance"));
                }
            }

            // quantity variance vs received
            ToleranceRule qtyRule = ruleMap.get(ToleranceRule.RuleType.QUANTITY_VARIANCE);
            BigDecimal tolerance = qtyRule != null && qtyRule.isActive()
                    ? qtyRule.getTolerancePct() : BigDecimal.ZERO;
            BigDecimal allowed = received.multiply(
                    BigDecimal.valueOf(100).add(tolerance)).divide(BigDecimal.valueOf(100),
                    3, RoundingMode.HALF_UP);
            if (invoiced.compareTo(allowed) > 0) {
                findings.add(new Finding(Type.OVER_BILLING, Severity.CRITICAL,
                        "Item " + line.getItemCode() + " invoiced " + invoiced
                                + " exceeds received " + received
                                + " beyond the " + tolerance + "% quantity tolerance"));
            } else if (invoiced.compareTo(received) > 0) {
                findings.add(new Finding(Type.QUANTITY_VARIANCE, Severity.WARNING,
                        "Item " + line.getItemCode() + " invoiced " + invoiced
                                + " exceeds received " + received + " within tolerance"));
            }
        }

        MatchStatus status = findings.stream().anyMatch(f -> f.severity == Severity.CRITICAL)
                ? MatchStatus.EXCEPTION : MatchStatus.MATCHED;
        return new MatchResult(status, findings);
    }

    public enum MatchStatus {
        MATCHED, EXCEPTION
    }

    public enum Severity {
        WARNING, CRITICAL
    }

    public record Finding(Type type, Severity severity, String detail) {
    }

    public record MatchResult(MatchStatus status, List<Finding> findings) {

        public MatchResult {
            findings = List.copyOf(findings);
        }

        public List<Finding> criticals() {
            return findings.stream().filter(f -> f.severity == Severity.CRITICAL).toList();
        }

        public List<Finding> warnings() {
            return findings.stream().filter(f -> f.severity == Severity.WARNING).toList();
        }
    }
}
