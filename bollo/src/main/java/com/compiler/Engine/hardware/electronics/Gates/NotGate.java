package com.compiler.Engine.hardware.electronics.Gates;

public class NotGate {
    private boolean input;

    public void setInput(boolean state){
        this.input = state;
    }

    public boolean getOutput(){
        return !this.input;
    }
}
