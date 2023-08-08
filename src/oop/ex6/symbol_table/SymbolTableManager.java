package oop.ex6.symbol_table;

import oop.ex6.compiler.Type;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * A facade class for managing a linked list of Symbol
 */
public class SymbolTableManager {
    private final LinkedList<SymbolTable> tables = new LinkedList<>();
    private final SymbolTable methodsTable = new SymbolTable();


    /**
     * Opens a new Symbol Table for a newly opened scope
     */
    public void openScope() {
        SymbolTable table = new SymbolTable();
        tables.addFirst(table);
    }

    /**
     * Getter for the linked list size
     * @return Size of linked list
     */
    public int getSize() {
        return tables.size();
    }

    /**
     * Removes the last element in the list
     */
    public void closeScope() {
        tables.removeFirst();
    }

    /**
     * Looks for a variable in the linked list, and returns it if found
     * @param name name of the variable to look for
     * @return the variable's data if found, false otherwise
     */
    public Data getParameter(String name) {
        Iterator<SymbolTable> tableIterator = tables.iterator();
        SymbolTable curTable = tableIterator.next();
        Data ret;
        while (tableIterator.hasNext()) {
            if ((ret = curTable.getParameter(name)) != null) {
                return ret;
            }
            curTable = tableIterator.next();
        }
        if ((ret = curTable.getParameter(name)) != null) {
            return ret;
        }
        return null;
    }

    /**
     * Looks for the wanted method in the methods table
     * @param name name of the variable to look for
     * @return the variable's data if found, false otherwise
     */
    public Data getMethod(String name) {
        return(methodsTable.getParameter(name));
    }

    /**
     * Adds a parameter to the current Symbol Table
     * @param name var name to add
     * @param type var type
     * @param isFinal true if the var is final, false otherwise
     * @return true if succeeded in adding, false otherwise
     */
    public boolean addParameter(String name, Type type, boolean isFinal) {
        return tables.getFirst().addParameter(name, type, isFinal);
    }

    /**
     * Adds a method to the current Symbol Table
     * @param name var name to add
     * @param type var type
     * @param isFinal true if the var is final, false otherwise
     * @return true if succeeded in adding, false otherwise
     */
    public boolean addMethod(String name, Type type, boolean isFinal) {
        return methodsTable.addParameter(name, type, isFinal);
    }

    public boolean isGlobal(String varName) {
        for (var table: tables) {
            if (tables.getLast() == table) {
                break;
            }
            if (table.getParameter(varName) != null) {
                return false;
            }
        }
        return tables.getLast().getParameter(varName) != null;
    }
}