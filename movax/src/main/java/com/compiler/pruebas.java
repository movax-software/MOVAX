package com.compiler;

import com.compiler.Engine.hardware.electronics.Gates.XorGate;
import com.compiler.Engine.hardware.electronics.blocks.FullAdder;
import com.compiler.Engine.hardware.electronics.blocks.HalfAdder;
import com.compiler.Engine.hardware.electronics.blocks.NBitAdder;

public class pruebas {

    
    public static void main(String[] args) {        
        XorGate xorGate = new XorGate();      
    
        xorGate.setInputs(false, true);
        System.out.println(xorGate.getOutput());

        HalfAdder halfAdder = new HalfAdder();

        halfAdder.setInputs(true, true);
        halfAdder.evaluate();
        System.out.println("sum: " + halfAdder.getSum());
        System.out.println("carry: " + halfAdder.getCarry());

        System.out.println("\nFull Adder");
       
        FullAdder fullAdder = new FullAdder();
        fullAdder.setInputs(false, false, true);
        fullAdder.evaluate();
        System.out.println("Output: \nSum: " + fullAdder.getSum() + "\nCarry: " + fullAdder.getCarryOut());

        System.out.println("\nNBit Adder");
       
        NBitAdder nBitAdder = new NBitAdder(8);
        boolean[] inputA = {false, true, false, false, false, true, false, false};
        boolean[] inputB = {false, false, false, true, false, false, false, true};

        nBitAdder.setInputs(inputA, inputB);
        nBitAdder.evaluate();

        boolean output[] = nBitAdder.getOutput();
        System.out.println("RESULTADO");
        for (boolean b : output) {
            System.out.print(b + " ");
        }
        System.out.println("Carry: " + nBitAdder.getCarryOut());

    }













    /* 
    private static int INC(int a){
        return a+1;
    }

    private static int DEC(int a){
        return a-1;
    }

    private static int MOV(int a, int b){
        return a = b;
    }

    private static int ADD(int a, int b){
        return a + b;
    }

    private static int SUB(int a, int b){
        return a - b;
    }
    */
}
