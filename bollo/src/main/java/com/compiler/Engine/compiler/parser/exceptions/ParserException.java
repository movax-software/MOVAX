package com.compiler.Engine.compiler.parser.exceptions;

public class ParserException extends Exception {
    
    private String msg = "";

    public ParserException(String msg) {
        this.msg = msg;
    }

    public ParserException(String message, String msg) {
        super(message);
        this.msg = msg;
    }

    public ParserException(Throwable cause, String msg) {
        super(cause);
        this.msg = msg;
    }

    public ParserException(String message, Throwable cause, String msg) {
        super(message, cause);
        this.msg = msg;
    }

    public ParserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
            String msg) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
    
}
