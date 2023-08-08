package oop.ex6.compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class for creating tokens out of a SJavac program and managing them
 */
public class SJavaTokenizer {
    public static final String PREFIX = "//";
    private static final String ADVANCE_REGEX = "\'.*\'|\".*\"|[+-]?\\d*\\.*\\d+|\\w+|[|]{2}|&{2}|\\S";
    private BufferedReader reader;
    private String currentToken;
    private String currentLine;
    private final Pattern p;
    private Matcher m;

    /**
     * Constructor
     * @param reader a buffered stream
     * @throws IOException throws IOException if file is missing or corrupt
     */
    public SJavaTokenizer(BufferedReader reader) throws IOException {
        this.reader = reader;
        this.p = Pattern.compile(ADVANCE_REGEX);
        advanceToStart();
    }

    /**
     * getter for current token
     * @return current token
     */
    public String getCurrentToken() {
        return currentToken;
    }

    /**
     * Advances the token until start of commands
     */
    public void advanceToStart() {
        advanceLine();
        if (currentLine != null)
            m = p.matcher(currentLine);
        advance();
    }

    /**
     * Sets the current file reader
     * @param reader file reader to set
     */
    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    /**
     * Advances currentToken to the next token
     */
    public void advance() {
        if (m != null && m.find()) {
            currentToken = currentLine.substring(m.start(), m.end());
        } else {
            advanceLine();
            if (currentLine == null) {
                return;
            }
            m = p.matcher(currentLine);
            advance();
        }
    }

    private void advanceLine() {
        try {
            read();
            while (currentLine != null &&
                    (currentLine.startsWith(PREFIX) || currentLine.isEmpty())) {
                read();
            }

        } catch (IOException e) {
            currentLine = null;
        }
    }

    private void read() throws IOException {
        currentLine = reader.readLine();
        if (currentLine != null && !currentLine.strip().startsWith(PREFIX)) {
            currentLine = currentLine.strip();
        }
    }

    /**
     * getter for current command
     * @return current command
     */
    public String getCurrentCommand() {
        return currentLine;
    }
}
