package com.compiler.Engine.hardware.electronics.Gates;

public class OrGate {
    private boolean input1 = false;
    private boolean input2 = false;

    public void setInputs(boolean state1, boolean state2){
        this.input1 = state1;
        this.input2 = state2;
    }

    public boolean getOutput(){
        return input1 || input2;
    }
}
