package com.herokuapp.ddspace.controller;

import com.herokuapp.ddspace.enums.ResultEnum;
import com.herokuapp.ddspace.exception.CustomizeException;
import com.herokuapp.ddspace.mapper.QuestionMapper;
import com.herokuapp.ddspace.model.Question;
import com.herokuapp.ddspace.model.User;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;

@Controller
@AllArgsConstructor
public class DeleteController {

    private QuestionMapper questionMapper;

    @GetMapping("/delete/question/{id}")
    public String deleteQuestion(@PathVariable("id") Integer id,
                                 HttpServletRequest request,
                                 Model model) {
        Question question = questionMapper.selectByPrimaryKey(id);
        if (question == null) {
            throw new CustomizeException(ResultEnum.QUESTION_NOT_FOUND);
        }
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            throw new CustomizeException(ResultEnum.NO_LOGIN);
        }
        if (!question.getCreator().equals(user.getId())) {
            throw new CustomizeException(ResultEnum.PERMISSION_ERROR);
        }
        questionMapper.deleteByPrimaryKey(id);
        model.addAttribute("redirect", "/");
        model.addAttribute("message", "删除成功");
        return "message";
    }
}
