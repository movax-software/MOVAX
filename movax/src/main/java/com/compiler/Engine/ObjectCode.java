/* Se creará un objeto ObjectCode para traducir el codigo ensamblador a binario.
 * 
 * Se trató de seguir la regla de que una instrucción de ensamblador es una instrucción 
 * en binario.
 * El constructor tiene como parámetro un boleano que indicará si se trata del mapeo de direcciones.
 * 
 * SALTO RELATIVO
 *  
 * Las instrucciónes de tipo CALL, JMP hacen referencia a la dirección de memoria dentro del 
 * segmento de código. Estas instrucciones se componen de un byte de OpCode y un byte para 
 * la cantidad de bytes de distancia hacia la dirección de destino. Por ejemplo: 
 * JMP LABEL1 = 1110 1011 0000 0100
 *             |_OpCode__|Distancia|
 * La distancia indica que se saltarán 4 bytes para llegar a la etiqueta LABEL1.
 * Esto tiene la problemática de que, la dirección donde se encuentra la etiqueta LABEL1
 * todavía no se genera cuando necesita ser referida.
 * La solución que encontré fue recorrer primero el codigo ensamblador mapeando las direcciónes
 * en un HashMap, cuando se encuentre una instrucción de salto relativo no generará el OpCode ni el
 * byte de distancia hacia la dirección de código, sino que sumará 2 bytes al contador de direcciones
 * para simular que esa línea está completa. Al terminar el mapeo todas las direcciones de etiquetas 
 * estarán guardadas correctamente en el HashMap estático "Adds".
 * En la segunda pasada cuando encuentre un CALL ya podrá hacer referencia a la dirección de código y 
 * calcular el salto relativo.
 */
package com.compiler.Engine;

import javax.swing.JTextArea;

public class ObjectCode {
    
    private final String[] CodigoASM;
    public StringBuilder Addresses = new StringBuilder();
    public StringBuilder Content = new StringBuilder();
    public StringBuilder Hex = new StringBuilder();
    
    public ObjectCode(JTextArea PanelASM) {
        this.CodigoASM = PanelASM.getText().split("\\n");
    }
    void Start(boolean primerPasada) {
        BinLine.primerPasada = primerPasada;

        if (primerPasada) { //Una linea de ASM, Una linea en Binario
            for (String ASMline : CodigoASM) {
                BinLine Line = new BinLine(ASMline);
                if (Line.isOmitible(ASMline)) continue;

                if (ASMline.contains("DB") || ASMline.contains("DW")) {
                    Line.GenerarData();
                } else {
                    Line.GenerarCode();
                }
            }
            BinLine.add = 0;
        } else {
            for (String ASMline : CodigoASM) {
                BinLine Line = new BinLine(ASMline);
                if (Line.isOmitible(ASMline)) {
                    //Hay instrucciones que no se transforman a binario pues son 
                    //herramientas del IDE (GUI TASM en este caso)
                    Addresses.append("\n");
                    Content.append("\n");
                    Hex.append("\n");
                    continue;
                }

                if (ASMline.contains(" DB ") || ASMline.contains(" DW ")) {
                    Line.GenerarData();
                } else {
                    Line.GenerarCode();
                }
                Addresses.append(Line.Address).append("\n");
                Content.append(Line.Content).append("\n");
                Hex.append(binarioAHex(Line.Address + Line.Content)).append("\n");
            }
        }
    }
    private String binarioAHex(String bin) {
        if (bin.contains("XXXX XXXX XXXX XXXX")) return "00 00 B8 XX XX";
        bin = bin.replaceAll(" ", "");

        while (bin.length() % 8 != 0) {
            bin = "0" + bin;
        }
        StringBuilder hexResult = new StringBuilder();
        for (int i = 0; i < bin.length(); i += 8) {
            String byteBin = bin.substring(i, i + 8);
            int decimal = Integer.parseInt(byteBin, 2);
            String hex = String.format("%02X", decimal);
            hexResult.append(hex).append(" ");
        }
        return hexResult.toString().trim(); 
    }
    //Getters y Setters
    public StringBuilder getAddresses() {
        return Addresses;
    }
    public void setAddresses(StringBuilder addresses) {
        Addresses = addresses;
    }
}
