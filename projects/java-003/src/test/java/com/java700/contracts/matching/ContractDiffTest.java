package com.java700.contracts.matching;

import static org.assertj.core.api.Assertions.assertThat;

import com.java700.contracts.matching.ContractDiff.ChangeType;
import java.util.List;
import org.junit.jupiter.api.Test;

class ContractDiffTest {

    private static final String V1 = """
            {"clauses":[
              {"number":"1.1","title":"Term","text":"12 months","sensitivity":1},
              {"number":"1.2","title":"Price","text":"100 USD per unit","sensitivity":3},
              {"number":"1.3","title":"Renewal","text":"Auto-renew 30 days","sensitivity":2}
            ]}""";

    private static final String V2 = """
            {"clauses":[
              {"number":"1.1","title":"Term","text":"12 months","sensitivity":1},
              {"number":"1.2","title":"Price","text":"105 USD per unit","sensitivity":3},
              {"number":"1.4","title":"Exit Right","text":"Terminate on 60 days notice","sensitivity":3}
            ]}""";

    @Test
    void detectsAddedRemovedModified() {
        List<ContractDiff.Change> changes = ContractDiff.diff(V1, V2);
        assertThat(changes).extracting(ContractDiff.Change::type)
                .containsExactlyInAnyOrder(ChangeType.ADDED, ChangeType.REMOVED, ChangeType.MODIFIED);
        assertThat(changes).filteredOn(c -> c.type() == ChangeType.ADDED)
                .extracting(ContractDiff.Change::number).containsExactly("1.4");
        assertThat(changes).filteredOn(c -> c.type() == ChangeType.REMOVED)
                .extracting(ContractDiff.Change::number).containsExactly("1.3");
        assertThat(changes).filteredOn(c -> c.type() == ChangeType.MODIFIED)
                .extracting(ContractDiff.Change::number).containsExactly("1.2");
    }

    @Test
    void identicalContentHasNoChanges() {
        assertThat(ContractDiff.diff(V1, V1)).isEmpty();
    }

    @Test
    void modifiedChangeCarriesOldAndNewText() {
        var change = ContractDiff.diff(V1, V2).stream()
                .filter(c -> c.type() == ChangeType.MODIFIED).findFirst().orElseThrow();
        assertThat(change.oldText()).isEqualTo("100 USD per unit");
        assertThat(change.newText()).isEqualTo("105 USD per unit");
    }

    @Test
    void malformedJsonThrows() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> ContractDiff.diff("not json", V2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void clauseParsingExtractsFields() {
        List<ContractDiff.ClauseRef> clauses = ContractDiff.clauses(V1);
        assertThat(clauses).hasSize(3);
        assertThat(clauses.get(1).sensitivity()).isEqualTo(3);
    }
}
