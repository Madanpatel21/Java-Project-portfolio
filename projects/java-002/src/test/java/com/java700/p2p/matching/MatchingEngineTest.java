package com.java700.p2p.matching;

import static org.assertj.core.api.Assertions.assertThat;

import com.java700.p2p.domain.GoodsReceipt;
import com.java700.p2p.domain.GoodsReceiptLine;
import com.java700.p2p.domain.Invoice;
import com.java700.p2p.domain.InvoiceLine;
import com.java700.p2p.domain.PurchaseOrder;
import com.java700.p2p.domain.PurchaseOrderLine;
import com.java700.p2p.domain.ToleranceRule;
import com.java700.p2p.matching.MatchingEngine.MatchStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MatchingEngineTest {

    private static final Instant NOW = Instant.parse("2026-08-20T00:00:00Z");

    private PurchaseOrder po;
    private PurchaseOrderLine line;
    private Invoice invoice;
    private List<ToleranceRule> rules;

    @BeforeEach
    void setUp() {
        po = new PurchaseOrder("po1", "PO-1001", "SUP-1", "Acme Supplies Ltd", "USD", NOW, NOW);
        line = new PurchaseOrderLine("pol1", "po1", 1, "WIDGET-2000", "Widget 2000",
                new BigDecimal("100"), new BigDecimal("10.0000"));
        invoice = new Invoice("inv1", "INV-5001", "SUP-1", "Acme Supplies Ltd", "USD",
                new BigDecimal("1000.00"), LocalDate.of(2026, 8, 1), null, NOW);
        rules = List.of(
                new ToleranceRule("r1", ToleranceRule.RuleType.PRICE_VARIANCE,
                        new BigDecimal("2.0"), "WARN"),
                new ToleranceRule("r2", ToleranceRule.RuleType.QUANTITY_VARIANCE,
                        new BigDecimal("5.0"), "WARN"),
                new ToleranceRule("r3", ToleranceRule.RuleType.AMOUNT_VARIANCE,
                        new BigDecimal("1.0"), "WARN"));
    }

    private InvoiceLine invLine(String code, String qty, String price) {
        return new InvoiceLine("il1", "inv1", code, new BigDecimal(qty),
                new BigDecimal(price), new BigDecimal(qty).multiply(new BigDecimal(price)));
    }

    private Map<String, List<GoodsReceipt>> receipts(String qty) {
        GoodsReceipt gr = new GoodsReceipt("gr1", "GR-9001", "po1", "SUP-1", NOW);
        GoodsReceiptLine grl = new GoodsReceiptLine("grl1", "gr1", "pol1", new BigDecimal(qty));
        return Map.of("po1", List.of(gr));
    }

    private Map<String, List<GoodsReceiptLine>> grLines(String qty) {
        return Map.of("gr1", List.of(new GoodsReceiptLine("grl1", "gr1", "pol1",
                new BigDecimal(qty))));
    }

    private MatchingEngine.MatchResult run(InvoiceLine invLine, String receivedQty) {
        return MatchingEngine.match(invoice, List.of(invLine), List.of(po),
                Map.of("po1", List.of(line)), receipts(receivedQty), grLines(receivedQty), rules);
    }

    @Test
    void exactThreeWayMatchPasses() {
        MatchingEngine.MatchResult result = run(invLine("WIDGET-2000", "100", "10.0000"), "100");
        assertThat(result.status()).isEqualTo(MatchStatus.MATCHED);
        assertThat(result.findings()).isEmpty();
    }

    @Test
    void itemCodeNormalizationMatchesAcrossFormatting() {
        MatchingEngine.MatchResult result = run(invLine("widget-2000", "100", "10.0000"), "100");
        assertThat(result.status()).isEqualTo(MatchStatus.MATCHED);
    }

    @Test
    void priceVarianceWithinToleranceIsWarningOnly() {
        MatchingEngine.MatchResult result = run(invLine("WIDGET-2000", "100", "10.1000"), "100");
        assertThat(result.status()).isEqualTo(MatchStatus.MATCHED);
        assertThat(result.warnings()).extracting(MatchingEngine.Finding::type)
                .contains(com.java700.p2p.domain.MatchException.Type.PRICE_VARIANCE);
    }

    @Test
    void priceVarianceBeyondToleranceIsCritical() {
        MatchingEngine.MatchResult result = run(invLine("WIDGET-2000", "100", "10.5000"), "100");
        assertThat(result.status()).isEqualTo(MatchStatus.EXCEPTION);
        assertThat(result.criticals()).extracting(MatchingEngine.Finding::type)
                .contains(com.java700.p2p.domain.MatchException.Type.PRICE_VARIANCE);
    }

    @Test
    void missingReceiptIsCritical() {
        MatchingEngine.MatchResult result = MatchingEngine.match(invoice,
                List.of(invLine("WIDGET-2000", "100", "10.0000")), List.of(po),
                Map.of("po1", List.of(line)), Map.of(), Map.of(), rules);
        assertThat(result.status()).isEqualTo(MatchStatus.EXCEPTION);
        assertThat(result.criticals()).extracting(MatchingEngine.Finding::type)
                .contains(com.java700.p2p.domain.MatchException.Type.MISSING_RECEIPT);
    }

    @Test
    void overBillingBeyondQuantityToleranceIsCritical() {
        MatchingEngine.MatchResult result = run(invLine("WIDGET-2000", "110", "10.0000"), "100");
        assertThat(result.status()).isEqualTo(MatchStatus.EXCEPTION);
        assertThat(result.criticals()).extracting(MatchingEngine.Finding::type)
                .contains(com.java700.p2p.domain.MatchException.Type.OVER_BILLING);
    }

    @Test
    void quantityVarianceWithinToleranceIsWarningOnly() {
        MatchingEngine.MatchResult result = run(invLine("WIDGET-2000", "103", "10.0000"), "100");
        assertThat(result.status()).isEqualTo(MatchStatus.MATCHED);
        assertThat(result.warnings()).extracting(MatchingEngine.Finding::type)
                .contains(com.java700.p2p.domain.MatchException.Type.QUANTITY_VARIANCE);
    }

    @Test
    void unknownItemCodeIsCritical() {
        MatchingEngine.MatchResult result = run(invLine("GADGET-9999", "10", "5.0000"), "100");
        assertThat(result.status()).isEqualTo(MatchStatus.EXCEPTION);
        assertThat(result.criticals()).extracting(MatchingEngine.Finding::type)
                .contains(com.java700.p2p.domain.MatchException.Type.NO_PO_MATCH);
    }

    @Test
    void currencyMismatchIsCritical() {
        Invoice eur = new Invoice("inv2", "INV-5002", "SUP-1", "Acme Supplies Ltd", "EUR",
                new BigDecimal("1000.00"), LocalDate.of(2026, 8, 1), null, NOW);
        MatchingEngine.MatchResult result = MatchingEngine.match(eur,
                List.of(invLine("WIDGET-2000", "100", "10.0000")), List.of(po),
                Map.of("po1", List.of(line)), receipts("100"), grLines("100"), rules);
        assertThat(result.status()).isEqualTo(MatchStatus.EXCEPTION);
    }

    @Test
    void supplierNameFuzzyMatchToleratesTypos() {
        Invoice fuzzy = new Invoice("inv3", "INV-5003", "SUP-1", "Acme Supplys Ltd", "USD",
                new BigDecimal("1000.00"), LocalDate.of(2026, 8, 1), null, NOW);
        MatchingEngine.MatchResult result = MatchingEngine.match(fuzzy,
                List.of(invLine("WIDGET-2000", "100", "10.0000")), List.of(po),
                Map.of("po1", List.of(line)), receipts("100"), grLines("100"), rules);
        assertThat(result.status()).isEqualTo(MatchStatus.MATCHED);
    }

    @Test
    void unknownSupplierIsCritical() {
        Invoice unknown = new Invoice("inv4", "INV-5004", "SUP-X", "Zeta Corp", "USD",
                new BigDecimal("1000.00"), LocalDate.of(2026, 8, 1), null, NOW);
        MatchingEngine.MatchResult result = MatchingEngine.match(unknown,
                List.of(invLine("WIDGET-2000", "100", "10.0000")), List.of(po),
                Map.of("po1", List.of(line)), receipts("100"), grLines("100"), rules);
        assertThat(result.status()).isEqualTo(MatchStatus.EXCEPTION);
        assertThat(result.criticals()).extracting(MatchingEngine.Finding::type)
                .contains(com.java700.p2p.domain.MatchException.Type.NO_PO_MATCH);
    }
}
