package oop.ex6.compiler;

/**
 * A class for an exception that was thrown because of invalid input
 */
public abstract class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
