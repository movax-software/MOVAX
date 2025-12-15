package com.compiler.Engine.hardware.electronics.blocks;

import com.compiler.Engine.hardware.electronics.Gates.OrGate;
import com.compiler.Engine.hardware.interfaces.Component;

public class FullAdder implements Component{
    private boolean inputA;
    private boolean inputB;
    private boolean carryIn;

    private HalfAdder halfAdder1 = new HalfAdder();
    private HalfAdder halfAdder2 = new HalfAdder();
    private OrGate orGate = new OrGate();
    
    private boolean sum;
    private boolean carryOut;

    public void setInputs(boolean stateA, boolean stateB, boolean stateCarry){
        this.inputA = stateA;
        this.inputB = stateB;
        this.carryIn = stateCarry;
    }


    public void evaluate(){
        this.halfAdder1.setInputs(inputA, inputB);
        this.halfAdder1.evaluate();
        
        this.halfAdder2.setInputs(halfAdder1.getSum(), carryIn);
        this.halfAdder2.evaluate();

        this.orGate.setInputs(this.halfAdder2.getCarry(), this.halfAdder1.getCarry());

        this.sum = this.halfAdder2.getSum();
        this.carryOut = this.orGate.getOutput();
    }

    public boolean getSum(){
        return this.sum;
    }

    public boolean getCarryOut(){
        return this.carryOut;
    }

}
