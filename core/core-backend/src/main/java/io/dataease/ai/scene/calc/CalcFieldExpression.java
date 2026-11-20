package io.dataease.ai.scene.calc;

import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

/** Single scalar expression. Every reference and function is resolved before it reaches a Provider. */
public final class CalcFieldExpression {
    private static final Set<String> FUNCTIONS = Set.of("ABS", "ROUND", "FLOOR", "CEIL", "NULLIF", "COALESCE");
    private static final Set<String> AGGREGATES = Set.of("SUM", "AVG", "MIN", "MAX", "COUNT");
    private final boolean isCross;
    private final String source;
    private final Function<String, String> references;
    private int position;
    private int tokens;

    private CalcFieldExpression(String source, boolean isCross, Function<String, String> references) {
        this.source = source;
        this.isCross = isCross;
        this.references = references;
    }

    public static String bind(String source, boolean isCross, Function<String, String> references) {
        if (source == null || source.isBlank() || source.length() > 2048) throw invalid();
        var parser = new CalcFieldExpression(source, isCross, references);
        String result = parser.expression(0);
        if (parser.peek() != 0 || parser.position != source.length()) throw invalid();
        return result;
    }

    private String expression(int depth) {
        String value = product(depth);
        while (peek() == '+' || peek() == '-') {
            char op = source.charAt(position++);
            value += " " + op + " " + product(depth);
        }
        return value;
    }

    private String product(int depth) {
        String value = atom(depth);
        while (peek() == '*' || peek() == '/' || peek() == '%') {
            char op = source.charAt(position++);
            value += " " + op + " " + atom(depth);
        }
        return value;
    }

    private String atom(int depth) {
        if (depth > 16 || ++tokens > 128) throw invalid();
        char next = peek();
        if (next == '+' || next == '-') {
            position++;
            return "(" + next + atom(depth + 1) + ")";
        }
        if (next == '(') {
            position++;
            String nested = expression(depth + 1);
            require(')');
            return "(" + nested + ")";
        }
        if (next == '[') {
            int end = source.indexOf(']', ++position);
            if (end <= position) throw invalid();
            String id = references.apply(source.substring(position, end));
            if (id == null || !id.matches("(?:params_)?-?[0-9]{1,20}")) throw invalid();
            position = end + 1;
            return "[" + id + "]";
        }
        int start = position;
        if (Character.isDigit(next) || next == '.') {
            while (position < source.length() && "0123456789.".indexOf(source.charAt(position)) >= 0) position++;
            String literal = source.substring(start, position);
            if (!literal.matches("(?:[0-9]{1,32}(?:\\.[0-9]{1,16})?|\\.[0-9]{1,16})")) throw invalid();
            return literal;
        }
        while (position < source.length() && Character.isLetter(source.charAt(position))) position++;
        String function = source.substring(start, position).toUpperCase(Locale.ROOT);
        if (isCross && function.equals("EXTRACT")) {
            require('(');
            requireWord("MONTH");
            requireWord("FROM");
            String date = expression(depth + 1);
            require(')');
            return "EXTRACT(MONTH FROM " + date + ")";
        }
        if (!isCross && function.equals("MONTH")) {
            require('(');
            String date = expression(depth + 1);
            require(')');
            return "MONTH(" + date + ")";
        }
        boolean aggregate = AGGREGATES.contains(function);
        if (!FUNCTIONS.contains(function) && !aggregate) throw invalid();
        require('(');
        StringBuilder args = new StringBuilder(expression(depth + 1));
        while (!aggregate && peek() == ',') {
            position++;
            args.append(", ").append(expression(depth + 1));
        }
        require(')');
        return function + "(" + args + ")";
    }

    private char peek() {
        while (position < source.length() && Character.isWhitespace(source.charAt(position))) position++;
        return position < source.length() ? source.charAt(position) : 0;
    }

    private void requireWord(String word) {
        peek();
        if (!source.regionMatches(true, position, word, 0, word.length())) throw invalid();
        position += word.length();
        if (position < source.length() && Character.isLetterOrDigit(source.charAt(position))) throw invalid();
    }

    private void require(char token) {
        if (peek() != token) throw invalid();
        position++;
    }

    static IllegalArgumentException invalid() { return new IllegalArgumentException("invalid_formula"); }
}
