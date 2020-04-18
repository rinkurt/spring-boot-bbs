package com.herokuapp.ddspace.advice;

import com.herokuapp.ddspace.exception.CustomizeException;
import com.herokuapp.ddspace.enums.ResultEnum;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class CustomizeExceptionHandler {

    @ExceptionHandler(Exception.class)
    ModelAndView handle (Throwable e, Model model) {
        if (e instanceof CustomizeException) {
            model.addAttribute("message", e.getMessage());
        } else {
            model.addAttribute("message", ResultEnum.UNKNOWN.getMessage());
            e.printStackTrace();
        }
        return new ModelAndView("error");
    }
}
