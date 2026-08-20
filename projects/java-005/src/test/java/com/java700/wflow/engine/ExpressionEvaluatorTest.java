package com.java700.wflow.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ExpressionEvaluatorTest {

    private static final Map<String, Object> VARS = Map.of(
            "amount", 5000,
            "currency", "USD",
            "approved", true);

    @Test
    void numericComparisons() {
        assertThat(ExpressionEvaluator.evaluate("var.amount > 1000", VARS)).isTrue();
        assertThat(ExpressionEvaluator.evaluate("var.amount <= 1000", VARS)).isFalse();
        assertThat(ExpressionEvaluator.evaluate("var.amount == 5000", VARS)).isTrue();
        assertThat(ExpressionEvaluator.evaluate("var.amount != 1", VARS)).isTrue();
        assertThat(ExpressionEvaluator.evaluate("var.amount >= 5000", VARS)).isTrue();
        assertThat(ExpressionEvaluator.evaluate("var.amount < 10", VARS)).isFalse();
    }

    @Test
    void stringComparisons() {
        assertThat(ExpressionEvaluator.evaluate("var.currency == \"USD\"", VARS)).isTrue();
        assertThat(ExpressionEvaluator.evaluate("var.currency == \"EUR\"", VARS)).isFalse();
        assertThat(ExpressionEvaluator.evaluate("var.currency != \"EUR\"", VARS)).isTrue();
    }

    @Test
    void booleanComparison() {
        assertThat(ExpressionEvaluator.evaluate("var.approved == true", VARS)).isTrue();
        assertThat(ExpressionEvaluator.evaluate("var.approved == false", VARS)).isFalse();
    }

    @Test
    void unsupportedExpressionThrows() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                ExpressionEvaluator.evaluate("var.x contains y", VARS))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
