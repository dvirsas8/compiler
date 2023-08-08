package oop.ex6.compiler;

import oop.ex6.validation.Validator;
import oop.ex6.symbol_table.*;

import java.util.ArrayList;

/**
 * A compiler for the initialization of the program
 */
public class Initializer extends SJavaCompiler {

    public static final String ILLEGAL_METHOD_DECLARATION_MSG = "Illegal method declaration";
    public static final String ILLEGAL_DECLARATION_MSG = "Illegal declaration";
    public static final String METHOD_ALREADY_DEFINED_MSG = "Already defined method with this name.";
    public static final String VOID = "void";

    /**
     * Constructor
     * @param tokenizer A tokenizer object to tokenize the program
     * @param manager A SymbolTableManager object to hold the program's symbol tables
     */
    public Initializer (SJavaTokenizer tokenizer, SymbolTableManager manager) {
        super(tokenizer, manager);
        currentToken = tokenizer.getCurrentToken();
    }

    /**
     * Runs the initialization of the program - finds method declarations and static variables
     * @throws ValidationException can throw validationException if met with syntax error
     */
    public void extractMethodsAndGlobalVariables() throws ValidationException {
        symbolTableManager.openScope();
        String currentCommand;
        while ((currentCommand = tokenizer.getCurrentCommand()) != null) {
            //method declaration
            if (currentCommand.startsWith(VOID)) {
                processMethodDeclaration();
            }
            //var declaration
            else if(currentCommand.startsWith(FINAL)|| startsWithType()){
                compileVariableDeclaration();
            }
            else if(Validator.checkVarName(currentToken)){
                String varName = currentToken;
                advance();
                compileLet(varName);
            }
            else{
                throw new UnexpectedStatementException(ILLEGAL_DECLARATION_MSG);
            }
        }
    }

    private boolean startsWithType(){
        for (var type : ALLOWED_TYPES) {
            if (tokenizer.getCurrentCommand().startsWith(type)) {
                return true;
            }
        }
        return false;
    }

    private void processMethodDeclaration() throws ValidationException {
        process(ALLOWED_RETURN_VALUE);
        String methodName = currentToken;

        //check if method name is legal
        if(!Validator.checkMethodName(currentToken)){
            throw new IllegalIdentifierException(ILLEGAL_METHOD_DECLARATION_MSG);
        }
        int openedScopes = 0, closedScopes = 0;

        //check if this method name wasn't used
        if(!symbolTableManager.addMethod(methodName, Type.METHOD, false)){
            throw new IllegalIdentifierException(METHOD_ALREADY_DEFINED_MSG);
        }
        advance();
        process(FIRST_PARENTHESES);

        //add signature to table
        ArrayList<Type> varTypes = compileParameterList();
        if (varTypes.size() > 0) {
            symbolTableManager.getMethod(methodName).setSignature(varTypes);
        }
        process(SECOND_PARENTHESES);
        process(SCOPE_FIRST_PARENTHESES);

        //Skip the method scope
        openedScopes++;
        while (closedScopes != openedScopes && tokenizer.getCurrentCommand() != null) {
            advance();
            if (currentToken.equals(SCOPE_SECOND_PARENTHESES)) closedScopes++;
            else if (currentToken.equals(SCOPE_FIRST_PARENTHESES)) openedScopes++;
        }
        process(SCOPE_SECOND_PARENTHESES);
    }
}
