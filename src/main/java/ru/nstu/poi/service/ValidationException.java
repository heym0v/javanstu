package ru.nstu.poi.service;

import java.util.List;

/** Данные сущности не прошли проверку. */
public class ValidationException extends Exception {
    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super(String.join("\n", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
