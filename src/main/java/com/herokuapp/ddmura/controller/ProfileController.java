package com.herokuapp.ddmura.controller;

import com.herokuapp.ddmura.dto.NotificationDTO;
import com.herokuapp.ddmura.dto.PaginationDTO;
import com.herokuapp.ddmura.dto.QuestionDTO;
import com.herokuapp.ddmura.model.User;
import com.herokuapp.ddmura.service.NotificationService;
import com.herokuapp.ddmura.service.QuestionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
@AllArgsConstructor
public class ProfileController {

    private QuestionService questionService;
    private NotificationService notificationService;

    @GetMapping("/profile/{action}")
    public String profile(@PathVariable(name = "action") String action,
                          Model model,
                          HttpServletRequest request,
                          @RequestParam(name = "page", defaultValue = "1") Integer page,
                          @RequestParam(name = "size", defaultValue = "10") Integer size) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "error";
        }
        if (action.equals("questions")) {
            model.addAttribute("section", "questions");
            model.addAttribute("sectionName", "我的提问");
            PaginationDTO<QuestionDTO> questionPagination = questionService.listByUser(user.getId(), page, size);
            model.addAttribute("pagination", questionPagination);
        } else if (action.equals("replies")) {
            model.addAttribute("section", "replies");
            model.addAttribute("sectionName", "最新回复");
            PaginationDTO<NotificationDTO> notificationPagination = notificationService.listByReceiver(user.getId(), page, size);
            model.addAttribute("pagination", notificationPagination);
        } else {
            model.addAttribute("section", "null");
            model.addAttribute("sectionName", "null");
            model.addAttribute("pagination", null);
        }
        return "profile";
    }
}
