package com.compiler.Engine;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assembly {

    TablaDeSimbolos TDS;        
    Semantico Sem;
        static StringBuilder CodigoASMEnsamblador = new StringBuilder();
        private ArrayList<String> Instrucciones = new ArrayList<>();
        private boolean MostrarRes = false;
        private boolean MostrarStr = false;
    
        public Assembly(Semantico sem){
            this.Sem = sem;
            this.TDS = sem.tds;        
        }
        public void Start(){
            Encabezado();
            TransformarDeclaraciones();
            TransformarExpresiones();
            Pie();
            MostrarProc();
            CodigoASMEnsamblador.append("END MAIN\n");
        }
        private void Encabezado(){
            CodigoASMEnsamblador.append(".MODEL SMALL\n.STACK 100H\n\n.DATA\n");
        }
        //.DATA
        private void TransformarDeclaraciones() {
            List<String> keys = new ArrayList<>(TDS.getTabla().keySet());
            
            for (String Key : keys) {
                Simbolo S = TDS.getTabla().get(Key);
                if (S.getTipo().equals("int")) {
                    CodigoASMEnsamblador.append(S.getNombre()).append(" DW ").append(S.getValor()).append("\n");
                } 
                if (S.getTipo().equals("float")) {
                    CodigoASMEnsamblador.append(S.getNombre()).append(" DD ").append(S.getValor()).append("      ").append("\n");
                }
                if (S.getTipo().equals("String")) {
                    if (S.getValor().equals("''")) {
                        CodigoASMEnsamblador.append(S.getNombre()).append(" DB ").append(" 256 DUP ('$')\n");    
                    } else {
                        CodigoASMEnsamblador.append(S.getNombre()).append(" DB ").append("10, 13,'").append(S.getValor()).append("'".replaceAll("\"", "\'")).append(", '$'\n");
                    }
                }
            }
        }
        //.CODE
    private void TransformarExpresiones() {
        CodigoASMEnsamblador.append("\n.CODE\nMAIN PROC\n\nMOV AX, @data\r\nMOV DS, AX\n\n");
        boolean bloqueIF=false;
        boolean bloqueElse=false;
        int ContadorIF = 0;
        for (String expr : Instrucciones) {
            expr = expr.trim();
            if (expr.startsWith("if")) {

                expr=expr.replace("if", "").trim();
                expr = expr.substring(1, expr.length()-1);
                String comp = extraerComparador(expr);
                String[] bloques = expr.split("==|!=|<=|>=|\\<|\\>");

                generarCodigoIf(bloques[0], comp, bloques[1], ContadorIF);
                bloqueIF = true;
                continue;
            }
            if (expr.startsWith("print")) {
                ProcesarPRINT(expr);
                continue;
            }
            if (expr.equals("{") && bloqueIF) {
                CodigoASMEnsamblador.append("\nLABEL_").append(ContadorIF).append(":\n");
                continue;
            }
            if (expr.equals("}") && bloqueIF) {
                CodigoASMEnsamblador.append("JMP FIN_").append(ContadorIF).append("\n");
                bloqueIF=false;
                continue;
            }
            if (expr.equals("}") && bloqueElse) {
                CodigoASMEnsamblador.append("\nFIN_").append(ContadorIF).append(":\n");
                bloqueElse=false;
                continue;
            }
            if (expr.equals("else")) {
                CodigoASMEnsamblador.append("\nELSELABEL_").append(ContadorIF).append(":\n");
                bloqueElse=true;
            }
            else {
                ProcesarASIG(expr);
            }
        }
        }
        private void Pie(){
            CodigoASMEnsamblador.append("\nMOV AH, 4Ch\r\nINT 21h\r\nMAIN ENDP\n\n");
        }
        private void ProcesarASIG(String expr){
                String Postfija = toPostfijo(expr);
                generarASM(Postfija);
                System.out.println(Postfija);
        }
        
        private static boolean isOperando(char c){
            return Character.isLetterOrDigit(c);
        }
        private static boolean isOperador(char c){
            return c == '+' | c == '-' | c == '*' | c == '/' | c == '=';
        }
        private String toPostfijo(String ExprInfija) {
    
            String[] tokens = TokenizarExpr(ExprInfija);
            Stack<Character> operadores = new Stack<>();
            StringBuilder posfija = new StringBuilder();
            
            for (String token : tokens) {
                char c = token.charAt(0);
    
                if (isOperando(c)) {
                    
                    posfija.append(token).append(" ");
                } else if (isOperador(c)) {
                   
                    while (!operadores.isEmpty() && precedencia(operadores.peek()) >= precedencia(c)) {
                        posfija.append(operadores.pop()).append(" ");
                    }
                    operadores.push(c);
                } else if (c == '(') {
                    operadores.push(c);
                } else if (c == ')') {
                    if (operadores.isEmpty()) {
                        throw new IllegalArgumentException("Paréntesis de cierre sin apertura.");
                    }
    
                    while (!operadores.isEmpty() && operadores.peek() != '(') {
                        posfija.append(operadores.pop()).append(" ");
                    }
    
                    if (!operadores.isEmpty()) {
                        operadores.pop(); // Eliminamos el '('
                    } else {
                        throw new IllegalArgumentException("Paréntesis de cierre sin apertura.");
                    }
                }
            }
    
            while (!operadores.isEmpty()) {
                char operador = operadores.pop();
               
                if (operador == '=') {
                    posfija.append(operador).append(" ");
                } else {
                    posfija.append(operador).append(" ");
                }
            }
    
            return posfija.toString().trim();
        }
        private static String generarASM(String expresionPosfija) {
            Stack<String> pila = new Stack<>();
            String[] tokens = expresionPosfija.split(" ");

            for (String token : tokens) {
                if (token.isBlank()) break;
                if (isOperando(token.charAt(0))) {
                    pila.push(token);
                } else if (isOperador(token.charAt(0))) {
                    String operando2 = pila.pop();
                    String operando1 = pila.pop();
        
                    switch (token) {
                        case "+" -> {
                            CodigoASMEnsamblador.append("MOV BX, ").append(operando1).append("\n");
                            CodigoASMEnsamblador.append("PUSH BX\n"); 
                            CodigoASMEnsamblador.append("MOV AX, ").append(operando2).append("\n");
                            CodigoASMEnsamblador.append("POP BX\n"); 
                            CodigoASMEnsamblador.append("ADD AX, BX\n"); 
                            pila.push("AX"); 
                        }
                        case "-" -> {
                            CodigoASMEnsamblador.append("MOV BX, ").append(operando1).append("\n");
                            CodigoASMEnsamblador.append("PUSH BX\n"); 
                            CodigoASMEnsamblador.append("MOV BX, ").append(operando2).append("\n");
                            CodigoASMEnsamblador.append("POP AX\n"); 
                            CodigoASMEnsamblador.append("SUB AX, BX\n"); 
                            pila.push("AX");
                        }
                        case "*" -> {
                            CodigoASMEnsamblador.append("MOV BX, ").append(operando1).append("\n");
                            CodigoASMEnsamblador.append("PUSH BX\n"); 
                            CodigoASMEnsamblador.append("MOV AX, ").append(operando2).append("\n");
                            CodigoASMEnsamblador.append("POP BX\n"); 
                            CodigoASMEnsamblador.append("IMUL BX\n"); 
                            pila.push("AX");
                        }
                        case "/" -> {
                            CodigoASMEnsamblador.append("MOV AX, ").append(operando1).append("\n");
                            CodigoASMEnsamblador.append("PUSH AX\n"); 
                            CodigoASMEnsamblador.append("MOV AX, ").append(operando2).append("\n");
                            CodigoASMEnsamblador.append("POP BX\n"); 
                            CodigoASMEnsamblador.append("MOV DX, 0\n"); 
                            CodigoASMEnsamblador.append("DIV BX\n"); 
                            pila.push("AX"); 
                        }
                        case "=" -> {
                            CodigoASMEnsamblador.append("MOV ").append(operando1).append(", ").append(operando2).append("\n");
                            pila.push(operando1);
                        }
                    }
                }
            }
            return "";
        }
    private static int precedencia(char operador) {
        return switch (operador) {
            case '+', '-' -> 1;
            case '*', '/' -> 2;
            case '=' -> 0;
            default -> -1;
        };
    }
    
    private String[] TokenizarExpr(String expr){
        Pattern patron = Pattern.compile("[a-zA-Z0-9]+|[\\+\\-\\*/=\\(\\)]");
        Matcher matcher = patron.matcher(expr);
        List<String> tokens = new ArrayList<>();

        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens.toArray(String[]::new);
    }
    public String generarCodigoIf(String operando1, String comparador, String operando2, int Contador) {
        String etiquetaSalto = null;
        CodigoASMEnsamblador.append("\n");

        CodigoASMEnsamblador.append(generarASM(toPostfijo(operando1)));
        CodigoASMEnsamblador.append("PUSH AX\n");

        CodigoASMEnsamblador.append(generarASM(toPostfijo(operando2)));
        CodigoASMEnsamblador.append("PUSH AX\n");

        CodigoASMEnsamblador.append("POP BX\n");
        CodigoASMEnsamblador.append("POP AX\n");
        CodigoASMEnsamblador.append("CMP AX, BX\n");
    
        switch (comparador) {

            case "==":
                etiquetaSalto = "JE ";
                break;
            case "!=":
                etiquetaSalto = "JNE ";
                break;
            case ">":
                etiquetaSalto = "JG ";
                break;
            case "<":
                etiquetaSalto = "JL ";
                break;
            case ">=":
                etiquetaSalto = "JGE ";
                break;
            case "<=":
                etiquetaSalto = "JLE ";
                break;
        }
        CodigoASMEnsamblador.append(etiquetaSalto).append(" LABEL_").append(Contador).append("\n");
        CodigoASMEnsamblador.append("JMP").append(" ELSELABEL_").append(Contador).append("\n");
        return "";

    }

    public static String extraerComparador(String expresion) {
        // Patrón para los comparadores comunes: ==, !=, <, >, <=, >=
        String patron = "(==|!=|<=|>=|<|>)";
        Pattern regex = Pattern.compile(patron);
        Matcher matcher = regex.matcher(expresion);
        if (matcher.find()) {
            return matcher.group(); // Retorna el comparador encontrado
        }
        return null; // No se encontró un comparador
    }

    private void ProcesarPRINT(String expr) {
        expr = expr.replace("print", "");
        expr = expr.substring(1, expr.length()-1);
        if (Sem.tds.getTabla().get(expr).getTipo().equals("String")) {        
            CodigoASMEnsamblador.append("LEA DX, ").append(expr).append("\n");
            CodigoASMEnsamblador.append("CALL MOSTRAR_STRING\n");
            MostrarStr=true;
        } else {
            CodigoASMEnsamblador.append("MOV AX, ").append(expr).append("\n");
            CodigoASMEnsamblador.append("CALL IMPRIMIR_NUMERO\n");
            MostrarRes=true;
        }
    }

    private void MostrarProc() {
        if (MostrarRes) {
            CodigoASMEnsamblador.append("IMPRIMIR_NUMERO PROC\r\n" + //
                                "    PUSH AX\r\n" + //
                                "    PUSH BX\r\n" + //
                                "    PUSH CX\r\n" + //
                                "    PUSH DX\r\n" + //
                                "    \r\n" + //
                                "    MOV CX, 0      \r\n" + //
                                "    MOV BX, 10     \r\n" + //
                                "\r\n" + //
                                "OBTENER_DIGITOS:\r\n" + //
                                "    MOV DX, 0      \r\n" + //
                                "    DIV BX         \r\n" + //
                                "    ADD DL, 30H    \r\n" + //
                                "    PUSH DX        \r\n" + //
                                "    INC CX         \r\n" + //
                                "    TEST AX, AX    \r\n" + //
                                "    JNZ OBTENER_DIGITOS\r\n" + //
                                "\r\n" + //
                                "MOSTRAR_DIGITOS:\r\n" + //
                                "    POP DX         \r\n" + //
                                "    MOV AH, 02h    \r\n" + //
                                "    INT 21h        \r\n" + //
                                "    LOOP MOSTRAR_DIGITOS \r\n" + //
                                "\r\n" + //
                                "    POP DX\r\n" + //
                                "    POP CX\r\n" + //
                                "    POP BX\r\n" + //
                                "    POP AX\r\n" + //
                                "    RET\r\n" + //
                                "    IMPRIMIR_NUMERO ENDP\n");
        }
        if (MostrarStr) {
            CodigoASMEnsamblador.append("MOSTRAR_STRING PROC\r\n" + //
                                "    MOV AH, 09h      \r\n" + //
                                "    INT 21h          \r\n" + //
                                "    RET              \r\n" + //
                                "    MOSTRAR_STRING ENDP\n");
        }
    }
}
    
