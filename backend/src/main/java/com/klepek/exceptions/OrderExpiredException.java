package com.klepek.exceptions;

public class OrderExpiredException extends RuntimeException {
    public OrderExpiredException(String message) {
        super(message);
    }
} 