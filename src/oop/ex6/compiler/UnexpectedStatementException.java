package oop.ex6.compiler;

public class UnexpectedStatementException extends ValidationException{
    public UnexpectedStatementException(String message) {
        super(message);
    }
}
