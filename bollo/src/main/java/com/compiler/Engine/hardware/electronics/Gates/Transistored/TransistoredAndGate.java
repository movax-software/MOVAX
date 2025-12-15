package com.compiler.Engine.hardware.electronics.Gates.Transistored;
import com.compiler.Engine.hardware.electronics.Transistors.Transistor;

public class TransistoredAndGate {

    private Transistor t1 = new Transistor();
    private Transistor t2 = new Transistor();

    private boolean input1;
    private boolean input2;

    public void setInputs(boolean a, boolean b) {
        this.input1 = a;
        this.input2 = b;
    }

    public boolean getOutput() {
        boolean vcc = true;

        t1.setGate(input1);
        t1.setSource(vcc);
        t1.evaluate();

        t2.setGate(input2);
        t2.setSource(t1.getDrain());
        t2.evaluate();

        return t2.getDrain();
    }
}
