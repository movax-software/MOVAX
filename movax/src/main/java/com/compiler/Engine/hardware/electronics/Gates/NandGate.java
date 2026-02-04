package com.compiler.Engine.hardware.electronics.Gates;

public class NandGate {
    private boolean input1 = false;
    private boolean input2 = false;

    private AndGate and = new AndGate();
    private NotGate not = new NotGate();

    public void setInputs(boolean state1, boolean state2){
        this.input1 = state1;
        this.input2 = state2;
    }

    public boolean getOutput(){
        this.and.setInputs(input1, input2);
        this.not.setInput(and.getOutput());
        
        return not.getOutput();
    }
}
