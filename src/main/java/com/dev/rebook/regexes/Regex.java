package com.dev.rebook.regexes;

public class Regex {
    public final String pattern;

    public Regex(String pattern) {
        this.pattern = pattern;
    }

    public boolean matches(String input) {
        return this.matches(input, false);
    }

    public boolean matches(String input, boolean allowEmptyNull) {
        if (input == null || input.isEmpty()) {
            return allowEmptyNull;
        }
        return input.matches(this.pattern);
    }
}
