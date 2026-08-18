package com.java700.stewardship.catalog;

import java.math.BigDecimal;
import java.util.List;

public final class DrugApi {

    private DrugApi() {
    }

    public record DrugView(String id, String code, String name, String drugClass, String spectrum,
                           BigDecimal dddGrams, boolean ivAvailable, boolean poAvailable,
                           boolean restricted, List<String> coverageTags,
                           BigDecimal ivCostPerDay, BigDecimal poCostPerDay) {

        public DrugView {
            coverageTags = List.copyOf(coverageTags);
        }

        static DrugView from(AntimicrobialDrug d) {
            return new DrugView(d.getId(), d.getCode(), d.getName(), d.getDrugClass(), d.getSpectrum(),
                    d.getDddGrams(), d.isIvAvailable(), d.isPoAvailable(), d.isRestricted(),
                    d.getCoverageTags(), d.getIvCostPerDay(), d.getPoCostPerDay());
        }
    }
}
