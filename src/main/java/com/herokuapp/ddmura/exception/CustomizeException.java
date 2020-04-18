package com.herokuapp.ddmura.exception;

import com.herokuapp.ddmura.enums.ResultEnum;

public class CustomizeException extends RuntimeException {

    @Override
    public String getMessage() {
        return result.getMessage();
    }

    public Integer getCode() {
        return result.getCode();
    }

    private ResultEnum result;

    public CustomizeException(ResultEnum result) {
        this.result = result;
    }
}
