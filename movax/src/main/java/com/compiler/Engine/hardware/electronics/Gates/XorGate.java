package com.compiler.Engine.hardware.electronics.Gates;

public class XorGate {
    
    private boolean input1 = false;
    private boolean input2 = false;
    private AndGate and = new AndGate();
    private OrGate or = new OrGate();
    private NandGate nand = new NandGate();

    public void setInputs(boolean state1, boolean state2){
        this.input1 = state1;
        this.input2 = state2;
    }

    public boolean getOutput(){
        this.or.setInputs(input1, input2);
        this.nand.setInputs(input1, input2);
        this.and.setInputs(or.getOutput(), nand.getOutput());

        return this.and.getOutput();
    }
}
