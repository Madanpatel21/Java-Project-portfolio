package com.java700.wflow.engine;

import java.util.Map;

/**
 * Minimal gateway condition evaluator: {@code var.name == "text"} or
 * {@code var.amount > 1000}. Supports ==, !=, >, >=, <, <= over numbers and strings.
 */
public final class ExpressionEvaluator {

    private ExpressionEvaluator() {
    }

    public static boolean evaluate(String expression, Map<String, Object> vars) {
        String expr = expression.trim();
        for (String op : new String[]{"==", "!=", ">=", "<=", ">", "<"}) {
            int idx = expr.indexOf(op);
            if (idx < 0) {
                continue;
            }
            String leftRaw = expr.substring(0, idx).trim();
            String rightRaw = expr.substring(idx + op.length()).trim();
            Object left = resolve(leftRaw, vars);
            Object right = resolve(rightRaw, vars);
            return compare(left, right, op);
        }
        throw new IllegalArgumentException("Unsupported expression: " + expression);
    }

    private static Object resolve(String token, Map<String, Object> vars) {
        if (token.startsWith("var.")) {
            String[] path = token.substring(4).split("\\.");
            Object current = vars;
            for (String part : path) {
                if (current instanceof Map<?, ?> map) {
                    current = map.get(part);
                } else {
                    return null;
                }
            }
            return current;
        }
        if (token.startsWith("\"") && token.endsWith("\"") && token.length() >= 2) {
            return token.substring(1, token.length() - 1);
        }
        if (token.matches("-?\\d+(\\.\\d+)?")) {
            return Double.parseDouble(token);
        }
        return token;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean compare(Object left, Object right, String op) {
        if (left instanceof Number ln && right instanceof Number rn) {
            double a = ln.doubleValue();
            double b = rn.doubleValue();
            return switch (op) {
                case "==" -> a == b;
                case "!=" -> a != b;
                case ">" -> a > b;
                case ">=" -> a >= b;
                case "<" -> a < b;
                case "<=" -> a <= b;
                default -> false;
            };
        }
        String a = String.valueOf(left);
        String b = String.valueOf(right);
        return switch (op) {
            case "==" -> a.equals(b);
            case "!=" -> !a.equals(b);
            case ">", ">=", "<", "<=" -> a.compareTo(b) != 0
                    && (op.equals(">") ? a.compareTo(b) > 0 : op.equals(">=") ? a.compareTo(b) >= 0
                    : op.equals("<") ? a.compareTo(b) < 0 : a.compareTo(b) <= 0);
            default -> false;
        };
    }
}
