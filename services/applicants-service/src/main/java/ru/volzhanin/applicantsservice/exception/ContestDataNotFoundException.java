package ru.volzhanin.applicantsservice.exception;

public class ContestDataNotFoundException extends RuntimeException {
    public ContestDataNotFoundException(String message) {
        super(message);
    }
}
