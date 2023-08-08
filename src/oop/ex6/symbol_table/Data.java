package oop.ex6.symbol_table;

import oop.ex6.compiler.Type;

import java.util.ArrayList;

/**
 * A nested class for all the values needed to define a variable
 */
public class Data {
    private final Type type;
    private final boolean isFinal;
    private ArrayList<Type> signature;
    private boolean isInitialized;

    /**
     * Constructor
     * @param type variable type
     * @param isFinal is variable final or not
     */
    public Data(Type type, boolean isFinal){
        this.type = type;
        this.isFinal = isFinal;
        this.signature = null;
        this.isInitialized = false;
    }

    /**
     * getter for variable's type
     * @return variable's type
     */
    public Type getType(){
        return this.type;
    }

    /**
     * sets the variable status to initialized
     */
    public void initialized() {
        isInitialized = true;
    }

    /**
     *
     * @return true if the variable is initialized, false otherwise
     */
    public boolean getInitialized() {
        return isInitialized;
    }

    /**
     * a getter for a method's signature
     * @return a list holding the needed types from the signature
     */
    public ArrayList<Type> getSignature(){
        return this.signature;
    }

    /**
     * getter for the isFinal field
     * @return true if the variable is final, false otherwise
     */
    public boolean isFinal(){
        return this.isFinal;
    }

    /**
     * setter for a method's signature
     * @param signature signature to set
     */
    public void setSignature(ArrayList<Type> signature){
        this.signature = signature;
    }
}


