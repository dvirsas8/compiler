package oop.ex6.compiler;
import oop.ex6.validation.Validator;
import oop.ex6.symbol_table.*;

import java.io.IOException;
import java.util.*;
import java.util.ArrayList;

/**
 * A class for compiling an SJava file
 */
public class CompilationEngine extends SJavaCompiler {

    private static final String SIGNATURE_DOESNT_MATCH_MSG = "Method signature doesn't match the given parameters";
    private static final String METHOD_DOESNT_EXIST_MSG = "Called to method that doesn't exist";
    private static final String ILLEGAL_EXPRESSION_MSG = "Illegal start of expression";
    private static final String NOT_BOOLEAN_TERM_MSG = "Not a boolean term";
    private static final String ILLEGAL_LINE_END_MSG = "Line has to end with one of [{,},;]";


    /**
     * Constructor
     * @param tokenizer a tokenizer object
     * @param manager a SymbolTableManager object
     */
    public CompilationEngine(SJavaTokenizer tokenizer, SymbolTableManager manager) {
        super(tokenizer, manager);

        currentToken = tokenizer.getCurrentToken();
    }

    /**
     * Runs the program main loop
     * @throws ValidationException can throw validationException if met with syntax error
     * @throws IOException can throw IOException if file is corrupted
     */
    public void run() throws ValidationException, IOException {
        tokenizer.advanceToStart();
        currentToken = tokenizer.getCurrentToken();
        while(tokenizer.getCurrentCommand() != null){
            compileStatements();
        }
    }

    private void compileScopeOpener(String ifOrWhile) throws ValidationException {
        symbolTableManager.openScope();

        process(ifOrWhile);

        //process boolean statement
        process(FIRST_PARENTHESES);
        compileBoolean();
        process(SECOND_PARENTHESES);

        //process body
        process(SCOPE_FIRST_PARENTHESES);
        while (!currentToken.equals(SCOPE_SECOND_PARENTHESES)) {
            compileStatements();
        }
        process(SCOPE_SECOND_PARENTHESES);

        symbolTableManager.closeScope();
    }

    private void compileMethod() throws ValidationException {
        symbolTableManager.openScope();
        process(ALLOWED_RETURN_VALUE);
        advance();
        process(FIRST_PARENTHESES);
        boolean isFinal = false, firstCheck = true;
        while (!Objects.equals(this.currentToken, SECOND_PARENTHESES)) {
            if (!firstCheck) process(SEPARATOR);
            firstCheck = false;
            if (currentToken.equals(FINAL)) {
                isFinal = true;
                process(FINAL);
            }
            Type type = Validator.convertToTypeEnum(currentToken);
            process(ALLOWED_TYPES);
            checkVarNameAndAdd(type, isFinal, true);
        }
        processMethodBody();
    }

    private void processMethodBody() throws ValidationException{
        process(SECOND_PARENTHESES);
        process(SCOPE_FIRST_PARENTHESES);
        compileBody();
        process(SCOPE_SECOND_PARENTHESES);
        symbolTableManager.closeScope();
    }

    private int compileStatements() throws ValidationException {
        //Checks if the file is over
        if (tokenizer.getCurrentCommand() == null) return 1;

        if (!(Validator.checkLegalLine(tokenizer.getCurrentCommand()) ||
                tokenizer.getCurrentCommand() == null))
            throw new UnexpectedStatementException(ILLEGAL_LINE_END_MSG);

        //Case var declaration
        if (isIn(ALLOWED_TYPES, currentToken) || currentToken.equals(FINAL)) {
            //If the var declaration is in the main scope it was already assigned
            // by the initializer,so skip.
            if (symbolTableManager.getSize() == MAIN_SCOPE) advanceLine();
            else compileVariableDeclaration();

            //If or while
        } else if (isIn(SCOPE_OPENERS, currentToken)) compileScopeOpener(currentToken);

            //Method
        else if (isIn(ALLOWED_RETURN_VALUE, currentToken)) {
            //If the var declaration is in the main scope it was already assigned by
            //the initializer, so skip.
            if (symbolTableManager.getSize() == MAIN_SCOPE) compileMethod();
            else throw new UnexpectedStatementException(ILLEGAL_EXPRESSION_MSG);

            //Let or call statement
        } else if ((Validator.checkVarName(currentToken) ||
                Validator.checkMethodName(currentToken)) && !currentToken.equals(RETURN)) {
            handleLetCall();

        } else if (currentToken.equals(RETURN) && symbolTableManager.getSize() == MAIN_SCOPE + 1) {
            process(RETURN);
            process(END_OF_LINE);
            if (currentToken.equals(SCOPE_SECOND_PARENTHESES)) return 2;

        } else if (currentToken.equals(RETURN)) {
            process(RETURN);
            process(END_OF_LINE);

        } else throw new UnexpectedStatementException(UNEXPECTED_CHARACTER_MSG);

        return 0;
    }

    private void handleLetCall() throws ValidationException {
        String varName = currentToken;
        advance();
        if (Objects.equals(currentToken, EQUAL_OP)) {
            if (symbolTableManager.getSize() == MAIN_SCOPE) advanceLine();
            else compileLet(varName);
        }
        else compileCall(varName);
    }

    private void compileBody() throws ValidationException {
        int flag = 0;
        while (flag == 0) {
            flag = compileStatements();
        }
    }

    private void compileCall(String varName) throws ValidationException {
        boolean firstCheck = true;
        int counter = 0;

        //Check if the call is to an existing method
        if (symbolTableManager.getMethod(varName) == null) {
            throw new ParameterException(METHOD_DOESNT_EXIST_MSG);
        }

        //Extracts the method signature
        ArrayList<Type> signature = symbolTableManager.getMethod(varName).getSignature();

        process(FIRST_PARENTHESES);

        //Checks if the signature corresponds the call
        while (!Objects.equals(currentToken, SECOND_PARENTHESES)) {
            if (counter > signature.size() - 1) {
                throw new ParameterException(SIGNATURE_DOESNT_MATCH_MSG);
            }
            checkSignature(firstCheck, signature, counter);
            firstCheck = false;
            counter++;
        }
        if (signature != null && counter < signature.size()) {
            throw new ParameterException(SIGNATURE_DOESNT_MATCH_MSG);
        }
        process(SECOND_PARENTHESES);
        process(END_OF_LINE);
    }

    private void checkSignature(boolean firstCheck, ArrayList<?> signature, int counter)
            throws ValidationException {
        if (!firstCheck) process(SEPARATOR);

        //two options- param we know or new value
        if (symbolTableManager.getParameter(currentToken) != null) {
            Data paramData = symbolTableManager.getParameter(currentToken);
            if (!allowType(paramData.getType(), (Type) signature.get(counter))) {
                throw new ParameterException(SIGNATURE_DOESNT_MATCH_MSG);
            }
            else if (!paramData.getInitialized()) {
                throw new ParameterException(UNINITIALIZED_PARAMETER_MSG);
            }
        } else {
            if (!Validator.checkType((Type) signature.get(counter), currentToken)) {
                throw new ParameterException(SIGNATURE_DOESNT_MATCH_MSG);
            }
        }
        advance();
    }

    private void compileBoolean() throws ValidationException {
        compileBooleanTerm();
        while (currentToken.equals(OR_OP) || currentToken.equals(AND_OP)) {
            advance();
            compileBooleanTerm();
        }
    }

    private void compileBooleanTerm() throws ValidationException {
        Data data;
        //option 1- known parameter, checks if bool.
        if ((data = symbolTableManager.getParameter(currentToken)) != null &&
                symbolTableManager.getParameter(currentToken).getInitialized()) {
            if (isIn(ALLOWED_BOOLEAN_TYPES, data.getType())) advance();
            else throw new UnexpectedStatementException(NOT_BOOLEAN_TERM_MSG);
        }

        else if (data != null &&
                !symbolTableManager.getParameter(currentToken).getInitialized()) {
            throw new ParameterException(UNINITIALIZED_PARAMETER_MSG);
        }

        //option 2- new value.
        else if (Validator.checkType(Type.BOOLEAN, currentToken)) {
            advance();

            //else, exception.
        } else {
            throw new UnexpectedStatementException(NOT_BOOLEAN_TERM_MSG);
        }
    }
}
