package oop.ex6.compiler;

public class IllegalIdentifierException extends ValidationException{
    public IllegalIdentifierException(String message) {
        super(message);
    }
}
