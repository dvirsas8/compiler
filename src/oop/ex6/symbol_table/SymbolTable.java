package oop.ex6.symbol_table;

import oop.ex6.compiler.Type;

import java.util.HashMap;

/**
 * A facade class for holding and managing a hashmap that links a varName to it's values
 */
public class SymbolTable {
    private final HashMap<String, Data> table;

    /**
     * Constructor
     */
    public SymbolTable(){
        this.table = new HashMap<>();
    }

    /**
     * adds a parameter to the symbol table
     * @param name varName to add
     * @param type var type
     * @param isFinal is the var final
     * @return true if succeeded adding, false otherwise
     */
    public boolean addParameter(String name, Type type, boolean isFinal){
        if (table.get(name) != null){
            return false;
        }
        Data data = new Data(type, isFinal);
        table.put(name, data);
        return true;
    }

    /**
     * getter for a parameter in the symbol table
     * @param name parameter name to look for
     * @return the parameter's data if found, null otherwise
     */
    public Data getParameter(String name){
        if (table.get(name) != null) {
            return table.get(name);
        }
        return null;
    }
}
