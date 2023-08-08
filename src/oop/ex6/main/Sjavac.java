package oop.ex6.main;

import oop.ex6.compiler.CompilationEngine;
import oop.ex6.compiler.Initializer;
import oop.ex6.compiler.SJavaTokenizer;
import oop.ex6.symbol_table.SymbolTableManager;
import oop.ex6.compiler.ValidationException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Compiles an SJava program
 */
public class Sjavac {
    private final static String NO_ERROR_PRINT = "0";
    private final static String VALIDATION_ERROR_PRINT = "1";
    private final static String FILE_ERROR_PRINT = "2";
    public static final String FILE_ERROR_MSG = "File corrupted or doesn't exist";
    public static final String WRONG_NUMBER_OF_ARGUMENTS_MSG = "Wrong number of arguments";

    /**
     * Main method
     *
     * @param args arguments from the command line
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println(FILE_ERROR_PRINT);
            System.out.println(WRONG_NUMBER_OF_ARGUMENTS_MSG);
        }
        String filename = args[0];
        try (FileReader fileReader = new FileReader(filename);
             BufferedReader reader1 = new BufferedReader(fileReader);
             FileReader fileReader2 = new FileReader(filename);
             BufferedReader reader2 = new BufferedReader(fileReader2)) {

            //Creates needed objects
            SJavaTokenizer tokenizer = new SJavaTokenizer(reader1);
            SymbolTableManager manager = new SymbolTableManager();
            Initializer initializer = new Initializer(tokenizer, manager);
            CompilationEngine compilationEngine = new CompilationEngine(tokenizer, manager);

            //Runs the compiler
            initializer.extractMethodsAndGlobalVariables();
            tokenizer.setReader(reader2);
            compilationEngine.run();
            reader1.close();
            reader2.close();
            System.out.println(NO_ERROR_PRINT);
        } catch (IOException e) {
            System.out.println(FILE_ERROR_PRINT);
            System.out.println(FILE_ERROR_MSG);
        } catch (ValidationException e) {
            System.out.println(VALIDATION_ERROR_PRINT);
            System.out.println(e.getMessage());
        }
    }
}