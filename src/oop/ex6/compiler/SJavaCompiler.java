package oop.ex6.compiler;

import oop.ex6.validation.Validator;
import oop.ex6.symbol_table.SymbolTableManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

/**
 * An abstract class representing a compiler that runs through an entire program
 */
public abstract class SJavaCompiler {
    //Exceptions
    static final String ILLEGAL_VARIABLE_NAME_MSG = "Illegal variable name";
    static final String UNEXPECTED_CHARACTER_MSG = "Unexpected Character";
    static final String UNDECLARED_PARAMETER_MSG = "Tried to access undeclared parameter";
    static final String WRONG_TYPE_MSG = "Assigned value not matching the variable's type";
    static final String UNINITIALIZED_PARAMETER_MSG = "Tried to use uninitialized parameter";
    static final String TRIED_TO_CHANGE_FINAL_MSG = "Tried to change a final variable";

    //Syntax
    final static Type[] ALLOWED_BOOLEAN_TYPES = {Type.BOOLEAN, Type.INT, Type.DOUBLE};
    final static String[] ALLOWED_TYPES = {"int", "double", "char", "boolean", "String"};
    final static String[] SCOPE_OPENERS = {"if", "while"};
    final static String[] ALLOWED_RETURN_VALUE = {"void"};
    final static String[] ALLOWED_PARAM_QUALIFIER = {"final"};
    final static String END_OF_LINE = ";";
    final static String SEPARATOR = ",";
    final static String FIRST_PARENTHESES = "(";
    final static String SECOND_PARENTHESES = ")";
    final static String SCOPE_FIRST_PARENTHESES = "{";
    final static String SCOPE_SECOND_PARENTHESES = "}";
    final static String RETURN = "return";
    final static String FINAL = "final";
    final static String OR_OP = "||";
    final static String AND_OP = "&&";
    final static String EQUAL_OP = "=";
    final static String ALREADY_ASSIGNED_MSG = "Tried to assign a taken variable name";

    //Define main scope
    public final static int MAIN_SCOPE = 1;
    public static final String EXPECTED_ONE_OF_MSG = "Expected one of: ";

    //Inner tools
    SJavaTokenizer tokenizer;
    SymbolTableManager symbolTableManager;
    String currentToken;

    /**
     * Constructor
     *
     * @param tokenizer A tokenizer object to tokenize the program
     * @param manager   A SymbolTableManager object to hold the program's symbol tables
     */
    public SJavaCompiler(SJavaTokenizer tokenizer, SymbolTableManager manager) {
        this.tokenizer = tokenizer;
        this.symbolTableManager = manager;

    }

    void compileVariableDeclaration() throws ValidationException {
        boolean isFinal = false, firstCheck = true;

        //May include a single param qualifier
        if (isIn(ALLOWED_PARAM_QUALIFIER, currentToken)) {
            isFinal = true;
            process(ALLOWED_PARAM_QUALIFIER);
        }

        //type
        Type type = Validator.convertToTypeEnum(currentToken);
        process(ALLOWED_TYPES);

        while (!Objects.equals(this.currentToken, END_OF_LINE)) {
            if (!firstCheck) {
                process(SEPARATOR);
            }
            firstCheck = false;
            String varName = currentToken;
            checkVarNameAndAdd(type, isFinal, false);
            if (isFinal) {
                process(EQUAL_OP);
                checkVarValueAndAdd(varName, currentToken);
            } else if (currentToken.equals(EQUAL_OP)) {
                advance();
                checkVarValueAndAdd(varName, currentToken);
            }
        }
        process(END_OF_LINE);
    }

    ArrayList<Type> compileParameterList() throws ValidationException {
        ArrayList<Type> signature = new ArrayList<>();
        boolean isFinal = false;
        boolean firstCheck = true;
        while (!Objects.equals(this.currentToken, SECOND_PARENTHESES)) {
            if (!firstCheck) {
                process(SEPARATOR);
            } else {
                firstCheck = false;
            }

            //allow a single param qualifier
            if (isIn(ALLOWED_PARAM_QUALIFIER, currentToken)) {
                isFinal = true;
                process(ALLOWED_PARAM_QUALIFIER);
            }

            Type type = Validator.convertToTypeEnum(currentToken);
            signature.add(type);
            process(ALLOWED_TYPES);
            if (!Validator.checkVarName(currentToken)) {
                throw new IllegalIdentifierException(ILLEGAL_VARIABLE_NAME_MSG);
            } else {
                advance();
            }
        }
        return signature;
    }

    void process(String[] expected) throws ValidationException {
        ArrayList<String> allowedInput = new ArrayList<>();
        Collections.addAll(allowedInput, expected);
        if (allowedInput.contains(this.currentToken)) {
            advance();
        } else {
            String err = EXPECTED_ONE_OF_MSG;
            for (var s : expected) {
                err += (s + ", ");
            }
            err += ". Got " + currentToken + " instead";
            throw new UnexpectedStatementException(err);
        }
    }

    void process(String toCheck) throws ValidationException {
        if (toCheck.equals(currentToken)) {
            advance();
        } else {
            throw new UnexpectedStatementException("Expected '" + toCheck + "', got '" + currentToken + "' instead");
        }
    }

    void advance() {
        tokenizer.advance();
        if (tokenizer.getCurrentCommand() == null) {
            currentToken = "";
            return;
        }
        currentToken = this.tokenizer.getCurrentToken();
    }

    void advanceLine() {
        while (!currentToken.equals(END_OF_LINE)) {
            advance();
        }
        advance();
    }

    boolean isIn(Object[] list, Object token) {
        ArrayList<Object> allowedInput = new ArrayList<>();
        Collections.addAll(allowedInput, list);
        return (allowedInput.contains(token));
    }

    void checkVarNameAndAdd(Type type, boolean isFinal, boolean isInitialized) throws ValidationException {
        if (!Validator.checkVarName(currentToken)) {
            throw new IllegalIdentifierException(ILLEGAL_VARIABLE_NAME_MSG);
        }
        if (!symbolTableManager.addParameter(currentToken, type, isFinal)) {
            throw new IllegalIdentifierException(ALREADY_ASSIGNED_MSG);
        }
        if (isInitialized) {
            symbolTableManager.getParameter(currentToken).initialized();
        }
        advance();
    }

    void checkVarValueAndAdd(String varName, String value) throws ValidationException {
        Type type;
        boolean flag;
        type = symbolTableManager.getParameter(varName).getType();
        //if it's a known parameter
        if (Validator.checkVarName(value)) {
            if (symbolTableManager.getParameter(value) != null) {
                if (symbolTableManager.getParameter(value).getInitialized()) {
                    flag = (allowType(type, symbolTableManager.getParameter(value).getType()));
                } else {
                    throw new ParameterException(UNINITIALIZED_PARAMETER_MSG);
                }
            } else {
                throw new ParameterException(UNDECLARED_PARAMETER_MSG);
            }
        } else {
            flag = Validator.checkType(type, value);
        }
        if (flag) {
            symbolTableManager.getParameter(varName).initialized();
            advance();
        } else {
            throw new ParameterException(WRONG_TYPE_MSG);
        }
    }

    void compileLet(String varName) throws ValidationException {
        boolean firstCheck = true;

        while (!Objects.equals(currentToken, END_OF_LINE)) {
            if (!firstCheck) {
                process(SEPARATOR);
                varName = currentToken;
                advance();
            }
            firstCheck = false;

            //Checks if the given var name is declared and final
            if (symbolTableManager.getParameter(varName) == null) {
                throw new ParameterException(UNDECLARED_PARAMETER_MSG);
            } else if (symbolTableManager.getParameter(varName).isFinal()) {
                throw new ParameterException(TRIED_TO_CHANGE_FINAL_MSG);
            }
            if (symbolTableManager.isGlobal(varName) && !symbolTableManager.getParameter(varName).getInitialized()) {
                var data = symbolTableManager.getParameter(varName);
                symbolTableManager.addParameter(varName, data.getType(), data.isFinal());
            }
            process(EQUAL_OP);
            checkVarValueAndAdd(varName, currentToken);
        }
        process(END_OF_LINE);
    }

    boolean allowType(Type type1, Type type2) {
        if (type1 == type2) {
            return true;
        }
        if (type1 == Type.DOUBLE) {
            if (type2 == Type.INT) {
                return true;
            }
        }
        if (type1 == Type.BOOLEAN) {
            return type2 == Type.DOUBLE || type2 == Type.INT;
        }
        return false;
    }

}
