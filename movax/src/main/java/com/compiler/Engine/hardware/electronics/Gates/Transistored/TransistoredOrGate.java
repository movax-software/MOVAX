package com.compiler.Engine.hardware.electronics.Gates.Transistored;
import com.compiler.Engine.hardware.electronics.Transistors.Transistor;

public class TransistoredOrGate {

    private Transistor t1 = new Transistor();
    private Transistor t2 = new Transistor();
    
    private boolean input1 = false;
    private boolean input2 = false;

    public void setInputs(boolean state1, boolean state2){
        this.input1 = state1;
        this.input2 = state2;
    }

    public boolean getOutput(){
        boolean vcc = true;

        t1.setGate(input1);
        t1.setSource(vcc);
        t1.evaluate();

        t2.setGate(input2);
        t2.setSource(vcc);
        t2.evaluate();

        return t1.getDrain() || t2.getDrain();
    }
}
