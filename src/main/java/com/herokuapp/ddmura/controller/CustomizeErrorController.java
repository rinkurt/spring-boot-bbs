package com.herokuapp.ddmura.controller;

import com.herokuapp.ddmura.enums.ResultEnum;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping({"/error"})
public class CustomizeErrorController implements ErrorController {


    @Override
    public String getErrorPath() {
        return "error";
    }

    @RequestMapping(
            produces = {"text/html"}
    )
    public ModelAndView errorHtml(HttpServletRequest request, Model model) {
        HttpStatus status = this.getStatus(request);
        if (status == HttpStatus.NOT_FOUND) {
            model.addAttribute("message", ResultEnum.NOT_FOUND.getMessage());
        } else if (status.is4xxClientError()) {
            model.addAttribute("message", ResultEnum.CLIENT_ERROR.getMessage());
        } else if (status.is5xxServerError()) {
            model.addAttribute("message", ResultEnum.SERVER_ERROR.getMessage());
        } else if (status.isError()) {
            model.addAttribute("message", ResultEnum.UNKNOWN.getMessage());
        }
        return new ModelAndView("error");
    }

    protected HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer)request.getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } else {
            try {
                return HttpStatus.valueOf(statusCode);
            } catch (Exception var4) {
                return HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
    }
}
