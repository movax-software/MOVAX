package com.compiler.Engine.hardware.electronics.blocks;

public class NBitAdder {
    private int bits;
    private boolean carryOut;
    private boolean[] output;
    private boolean[] inputA;
    private boolean[] inputB;

    public NBitAdder(int bits){
        this.bits = bits;
        this.output = new boolean[bits];
        this.inputA = new boolean[bits];
        this.inputB = new boolean[bits];
    }

    public void setInputs(boolean[] inputA, boolean[] inputB){
        if (inputA.length != bits || inputB.length != bits) 
            throw new IllegalArgumentException("Tamaño de operandos incorrecto");
        
        for (int i = 0; i < bits; i++) {
            this.inputA[i] = inputA[i];
            this.inputB[i] = inputB[i];
        }
    }

    public void evaluate(){
        boolean carry = false;
        this.output = new boolean[this.bits];

        for (int i = 0; i < bits; i++) {
            FullAdder fullAdder = new FullAdder();

            fullAdder.setInputs(inputA[i], inputB[i], carry);
            fullAdder.evaluate();

            output[i] = fullAdder.getSum();
            carry = fullAdder.getCarryOut();
        }
        
        this.carryOut = carry;

    }

    public boolean[] getOutput(){
        return this.output;
    }
    
    public boolean getCarryOut(){
        return carryOut;
    }
}
