package com.herokuapp.ddmura.controller;

import com.herokuapp.ddmura.dto.LikeNotifyDTO;
import com.herokuapp.ddmura.dto.NotificationDTO;
import com.herokuapp.ddmura.dto.PaginationDTO;
import com.herokuapp.ddmura.dto.QuestionDTO;
import com.herokuapp.ddmura.enums.LoginMessage;
import com.herokuapp.ddmura.enums.ResultEnum;
import com.herokuapp.ddmura.exception.CustomizeException;
import com.herokuapp.ddmura.mapper.UserMapper;
import com.herokuapp.ddmura.model.User;
import com.herokuapp.ddmura.model.UserExample;
import com.herokuapp.ddmura.service.LikeService;
import com.herokuapp.ddmura.service.NotificationService;
import com.herokuapp.ddmura.service.QuestionService;
import com.herokuapp.ddmura.service.UserService;
import jodd.crypt.BCrypt;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@AllArgsConstructor
public class ProfileController {

    private QuestionService questionService;
    private NotificationService notificationService;
    private LikeService likeService;
    private UserMapper userMapper;
    private UserService userService;

    @GetMapping("/profile/{action}")
    public String profile(@PathVariable(name = "action") String action,
                          Model model,
                          HttpServletRequest request,
                          @RequestParam(name = "page", defaultValue = "1") Integer page,
                          @RequestParam(name = "size", defaultValue = "10") Integer size) {
        Object obj = request.getSession().getAttribute("user");
        if (!(obj instanceof User)) {
            throw new CustomizeException(ResultEnum.NO_LOGIN);
        }
        User user = (User) obj;
        switch (action) {
            case "update":
                return "update_profile";
            case "questions":
                model.addAttribute("section", "questions");
                model.addAttribute("sectionName", "我的提问");
                PaginationDTO<QuestionDTO> questionPagination = questionService.listByUser(user.getId(), page, size);
                model.addAttribute("pagination", questionPagination);
                break;
            case "replies":
                model.addAttribute("section", "replies");
                model.addAttribute("sectionName", "最新回复");
                PaginationDTO<NotificationDTO> notificationPagination = notificationService.listByReceiver(user.getId(), page, size);
                model.addAttribute("pagination", notificationPagination);
                break;
            case "likes":
                model.addAttribute("section", "likes");
                model.addAttribute("sectionName", "收到的赞");
                List<LikeNotifyDTO> likeList = likeService.listLikeByReceiver(user.getId());
                model.addAttribute("likeList", likeList);
                break;
            default:
                model.addAttribute("section", "null");
                model.addAttribute("sectionName", "null");
                model.addAttribute("pagination", null);
                break;
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

    @PostMapping("/profile/update")
    public String updateUser(@RequestParam("username") String username,
                             @RequestParam("name") String name,
                             @RequestParam("avatar") String avatar,
                             @RequestParam("password") String password,
                             @RequestParam("re_password") String rePassword,
                             @RequestParam("bio") String bio,
                             HttpServletRequest request,
                             Model model) {
        Object obj = request.getSession().getAttribute("user");
        if (!(obj instanceof User)) {
            throw new CustomizeException(ResultEnum.NO_LOGIN);
        }
        User sessionUser = (User) obj;
        if (!StringUtils.isEmpty(password) && !password.equals(rePassword)) {
            model.addAttribute("error", LoginMessage.PASSWORD_NOT_MATCH);
            return "update_profile";
        }
        if (password != null && password.length() > 30) {
            model.addAttribute("error", LoginMessage.PASSWORD_TOO_LONG);
            return "update_profile";
        }
        UserExample example = new UserExample();
        example.createCriteria().andUsernameEqualTo(username);
        List<User> users = userMapper.selectByExample(example);
        if (!sessionUser.getUsername().equals(username) && users != null && users.size() != 0) {
            model.addAttribute("error", LoginMessage.USERNAME_EXISTS);
            return "update_profile";
        }

        User user = new User();
        user.setId(sessionUser.getId());
        if (!StringUtils.isEmpty(username)) {
            user.setUsername(username);
        }
        if (!StringUtils.isEmpty(password)) {
            user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        }
        if (!StringUtils.isEmpty(name)) {
            user.setName(name);
        }
        if (!StringUtils.isEmpty(avatar)) {
            user.setAvatarUrl(avatar);
        }
        user.setBio(bio);
        user = userService.updateById(user);
        request.getSession().setAttribute("user", user);

        model.addAttribute("message", "修改成功");
        model.addAttribute("redirect", "/");
        return "message";
    }
}
