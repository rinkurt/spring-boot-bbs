package com.herokuapp.ddspace.controller;

import com.herokuapp.ddspace.dto.LikeNotifyDTO;
import com.herokuapp.ddspace.dto.NotificationDTO;
import com.herokuapp.ddspace.dto.PaginationDTO;
import com.herokuapp.ddspace.dto.QuestionDTO;
import com.herokuapp.ddspace.model.User;
import com.herokuapp.ddspace.service.LikeService;
import com.herokuapp.ddspace.service.NotificationService;
import com.herokuapp.ddspace.service.QuestionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

@Controller
@AllArgsConstructor
public class ProfileController {

    private QuestionService questionService;
    private NotificationService notificationService;
    private LikeService likeService;

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
        } else if (action.equals("likes")) {
            model.addAttribute("section", "likes");
            model.addAttribute("sectionName", "收到的赞");
            List<LikeNotifyDTO> likeList = likeService.listLikeByReceiver(user.getId());
            model.addAttribute("likeList", likeList);
        } else {
            model.addAttribute("section", "null");
            model.addAttribute("sectionName", "null");
            model.addAttribute("pagination", null);
        }
        return "profile";
    }

    @GetMapping("/profile/likes/{id}")
    public String listLikeUser(@PathVariable("id") Integer id,
                               @RequestParam("type") Integer type,
                               Model model) {
        List<User> likeUserList = likeService.getLikeUserList(id, type);
        model.addAttribute("section", "likeList");
        model.addAttribute("sectionName", "点赞详情");
        model.addAttribute("likeUserList", likeUserList);
        return "profile";
    }
}
