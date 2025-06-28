package com.dev.rebook.results;

public interface Result {
    String name();

    String toString();

    default String toStringLower() {
        return this.toString().toLowerCase();
    }
}
