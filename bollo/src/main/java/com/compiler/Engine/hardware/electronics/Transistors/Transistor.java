package com.compiler.Engine.hardware.electronics.Transistors;

public class Transistor {

    private boolean gate;
    private boolean source;
    private boolean drain;

    public void setGate(boolean gate) {
        this.gate = gate;
    }

    public void setSource(boolean source) {
        this.source = source;
    }

    public boolean getDrain() {
        return drain;
    }

    public void evaluate() {
        drain = gate && source;
    }
}


