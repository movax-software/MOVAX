package com.compiler.Engine.hardware.electronics.blocks;

import com.compiler.Engine.hardware.electronics.Gates.*;
import com.compiler.Engine.hardware.interfaces.Component;

public class HalfAdder implements Component{
    private boolean inputA;
    private boolean inputB;

    private XorGate xor = new XorGate();
    private AndGate and = new AndGate();

    private boolean sum;
    private boolean carry;

    public void setInputs(boolean stateA, boolean stateB){
        this.inputA = stateA;
        this.inputB = stateB;
    }

    public void evaluate(){
        this.xor.setInputs(inputA, inputB);
        this.and.setInputs(inputA, inputB);

        this.sum = xor.getOutput();
        this.carry = and.getOutput();
    }

    public boolean getSum(){
        return this.sum;
    }

    public boolean getCarry(){
        return this.carry;
    }
}
