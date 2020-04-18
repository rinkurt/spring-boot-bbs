package com.herokuapp.ddspace.exception;

import com.herokuapp.ddspace.enums.ResultEnum;

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
