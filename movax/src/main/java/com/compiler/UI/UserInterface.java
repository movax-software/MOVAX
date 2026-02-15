package com.compiler.UI;

import com.compiler.Engine.compiler.escaner.Escaner;
import com.compiler.Engine.compiler.escaner.Token;
import com.compiler.Engine.compiler.parser.Parser;
import com.compiler.Engine.compiler.parser.exceptions.ParserException;

public class UserInterface {

    public static void main(String[] args) {
        Escaner esc = new Escaner("""
                #include <stdio.h>
                int main(){
                    int x = 90;
                    
                    if ((x) > 50+x) {
                        printf("x es mayor que 50");
                    } else {
                        printf("x es menor o igual a 50");
                    }
                    
                    for(int i=0;(i+3)<10;i++){
                        int f = 10;
                        printf(((f+i)*a));
                    }

                    y = 2*(x+4)+a+a;
                    print(y);
                    
                    if ((2*(x+4)+a) < (200+(a*a)-1)){
                        x=3;
                        print(MSG1);
                    } else {
                        x=6;
                        printf(MSG2);
                    }
                    x=10;
                    printf(x);
                }
                """);
            esc.Scan();

            for (Token token : esc.Tokens) {
                System.out.println(token.getTokenType() + "         " + token.getLexema());
            }
            Parser prsr = new Parser(esc.Tokens);
            try {
                prsr.parse();
            } catch (ParserException e) {
                e.printStackTrace();
            }
        
    }
}
