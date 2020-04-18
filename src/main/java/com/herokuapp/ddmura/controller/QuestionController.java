package com.herokuapp.ddmura.controller;

import com.herokuapp.ddmura.dto.CommentDTO;
import com.herokuapp.ddmura.dto.CommentType;
import com.herokuapp.ddmura.dto.QuestionDTO;
import com.herokuapp.ddmura.mapper.QuestionExtMapper;
import com.herokuapp.ddmura.model.Question;
import com.herokuapp.ddmura.model.User;
import com.herokuapp.ddmura.service.CommentService;
import com.herokuapp.ddmura.service.QuestionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@AllArgsConstructor
public class QuestionController {

    private QuestionService questionService;
    private QuestionExtMapper questionExtMapper;
    private CommentService commentService;

    @GetMapping("/question/{id}")
    public String question(@PathVariable(name = "id") Integer id, Model model, HttpServletRequest request) {
        User sessionUser = (User) request.getSession().getAttribute("user");
        questionService.incView(id, 1);
        QuestionDTO questionDto = questionService.findById(id, sessionUser);
        List<CommentDTO> comments = commentService.findByParent(id, CommentType.QUESTION, sessionUser);
        model.addAttribute("question", questionDto);
        model.addAttribute("comments", comments);
        StringBuilder sb = new StringBuilder();
        for (String tag : questionDto.getTag().split(",")) {
            if (sb.length() > 0) {
                sb.append('|');
            }
            sb.append(tag.trim());
        }
        List<Question> relatedQuestions = questionExtMapper.selectRelated(id, sb.toString());
        model.addAttribute("relatedQuestions", relatedQuestions);
        return "question";
    }
}
