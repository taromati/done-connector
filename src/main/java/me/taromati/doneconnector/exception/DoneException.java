package me.taromati.doneconnector.exception;

import lombok.Getter;

@Getter
public class DoneException extends RuntimeException {
    private final String code;
    private final String message;

    public DoneException(ExceptionCode code) {
        super(code.getMessage());

        this.message = code.getMessage();
        this.code = code.getCode();
    }

    public DoneException(String code, String message) {
        super(message);

        this.message = message;
        this.code = code;
    }
}
