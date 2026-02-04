package com.compiler.Engine.hardware.electronics.Gates.Transistored;

import com.compiler.Engine.hardware.electronics.Transistors.Transistor;

public class TransistoredNotGate {

    private Transistor t = new Transistor();
    private boolean input;

    public void setInput(boolean state) {
        this.input = state;
    }

    public boolean getOutput() {
        boolean gnd = false;

        t.setGate(input);
        t.setSource(gnd);
        t.evaluate();

        return !t.getDrain();
    }
}
