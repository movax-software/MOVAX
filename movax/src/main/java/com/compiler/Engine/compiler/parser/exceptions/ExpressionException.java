package com.compiler.Engine.compiler.parser.exceptions;

public class ExpressionException extends ParserException{

    public ExpressionException(String msg) {
        super(msg);
    }

    public ExpressionException(String message, String msg) {
        super(message, msg);
    }

    public ExpressionException(Throwable cause, String msg) {
        super(cause, msg);
    }

    public ExpressionException(String message, Throwable cause, String msg) {
        super(message, cause, msg);
    }

    public ExpressionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
            String msg) {
        super(message, cause, enableSuppression, writableStackTrace, msg);
    }
    
}
