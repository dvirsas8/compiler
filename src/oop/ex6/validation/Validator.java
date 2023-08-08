package oop.ex6.validation;

import oop.ex6.compiler.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class for validating different program inputs
 */
public class Validator {
    // Regex constants
    private final static String VAR_NAME_REGEX = "(^_+\\w+|^[a-zA-Z]+\\w*)";
    private final static String METHOD_NAME_REGEX = "[a-zA-Z]+[\\w]*[ \\t]*";
    private final static String DOUBLE_REGEX = "[+-]?\\d*\\.*\\d+";
    private final static String INT_REGEX = "[+-]?\\d+";
    private final static String CHAR_REGEX = "'.{1}'";
    private final static String BOOLEAN_REGEX = "true|false";
    private final static String STRING_REGEX = "\".*\"";
    public static final String LEGAL_LINE_REGEX = "([}{;]\\s*)$";
    public static final String DEC_COUNT_REGEX = "([^;]*;)|([^{]*\\{)|([^}]*})";

    // Type constants
    final static String STRING_TYPE = "String";
    final static String INT_TYPE = "int";
    final static String DOUBLE_TYPE = "double";
    final static String CHAR_TYPE = "char";
    final static String BOOLEAN_TYPE = "boolean";
    static String[] ILLEGAL_NAMES = {"String", "int", "double", "char", "boolean", "if", "while", "return",
            "true", "false", "void", "final"};

    /**
     * Receives regex and a value and checks if they match
     *
     * @param regex a regular expression
     * @param value value to compare with
     * @return true if there's a match, false otherwise
     */
    public static boolean editRegex(String regex, String value) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(value);
        return m.matches();
    }

    /**
     * Checks if the given value is of the correct type
     *
     * @param type  wanted type
     * @param value value to compare
     * @return true if the type matches, false otherwise
     */
    public static boolean checkType(Type type, String value) {
        Boolean flag = false;
        if (type == Type.STRING) {
            flag = editRegex(STRING_REGEX, value);
        }
        if (type == Type.DOUBLE || type == Type.BOOLEAN) {
            flag = editRegex(DOUBLE_REGEX, value);
            if (flag) {
                return true;
            }
        }
        if (type == Type.INT || type == Type.BOOLEAN || type == Type.DOUBLE) {
            flag = editRegex(INT_REGEX, value);
            if (flag) {
                return true;
            }
        }
        if (type == Type.BOOLEAN) {
            flag = editRegex(BOOLEAN_REGEX, value);
        }
        if (type == Type.CHAR) {
            flag = editRegex(CHAR_REGEX, value);
        }
        return flag;
    }

    /**
     * checks if a varName is legal
     *
     * @param varName name to check
     * @return true if legal, false otherwise
     */
    static public boolean checkVarName(String varName) {
        Pattern p = Pattern.compile(VAR_NAME_REGEX);
        Matcher m = p.matcher(varName);
        return (m.matches() && !isIn(ILLEGAL_NAMES, varName));
    }

    /**
     * Checks if a method name is legal
     *
     * @param methodName method name to check
     * @return true if legal, false otherwise
     */
    static public boolean checkMethodName(String methodName) {
        Pattern p = Pattern.compile(METHOD_NAME_REGEX);
        Matcher m = p.matcher(methodName);
        return (m.matches() && !isIn(ILLEGAL_NAMES, methodName));
    }

    /**
     * Converts a string to it's matching enum
     *
     * @param type string to check
     * @return the matching type if found, null otherwise
     */
    public static Type convertToTypeEnum(String type) {
        if (Objects.equals(type, STRING_TYPE)) {
            return Type.STRING;
        } else if (Objects.equals(type, BOOLEAN_TYPE)) {
            return Type.BOOLEAN;
        } else if (Objects.equals(type, INT_TYPE)) {
            return Type.INT;
        } else if (Objects.equals(type, CHAR_TYPE)) {
            return Type.CHAR;
        } else if (Objects.equals(type, DOUBLE_TYPE)) {
            return Type.DOUBLE;
        }
        return null;
    }

    /**
     * Checks if a line is legal
     *
     * @param currentCommand line to check
     * @return true if legal, false otherwise
     */
    public static boolean checkLegalLine(String currentCommand) {
        Pattern p = Pattern.compile(LEGAL_LINE_REGEX);
        Matcher m = p.matcher(currentCommand);
        return (m.find() && checkMaxOneDec(currentCommand));
    }

    public static boolean isIn(Object[] list, Object token) {
        ArrayList<Object> allowedInput = new ArrayList<>();
        Collections.addAll(allowedInput, list);
        return (allowedInput.contains(token));
    }

    public static boolean checkMaxOneDec(String currentCommand) {
        Pattern pattern = Pattern.compile(DEC_COUNT_REGEX);
        Matcher matcher = pattern.matcher(currentCommand);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count <= 1;
    }
}
