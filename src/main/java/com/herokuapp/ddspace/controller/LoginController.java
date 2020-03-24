package com.herokuapp.ddspace.controller;

import com.herokuapp.ddspace.enums.LoginMessage;
import com.herokuapp.ddspace.enums.ResultEnum;
import com.herokuapp.ddspace.exception.CustomizeException;
import com.herokuapp.ddspace.mapper.UserMapper;
import com.herokuapp.ddspace.model.User;
import com.herokuapp.ddspace.model.UserExample;
import com.herokuapp.ddspace.service.UserService;
import jodd.crypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@Controller
public class LoginController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        User user = (User) request.getSession().getAttribute("user");
        if (user != null) {
            throw new CustomizeException(ResultEnum.REPETITIVE_LOGIN);
        }
        model.addAttribute("clientId", System.getenv("GITHUB_CLIENT_ID"));
        model.addAttribute("redirectUri", System.getenv("GITHUB_CALLBACK_URL"));
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          Model model,
                          HttpServletResponse response) {
        if (StringUtils.isEmpty(username)) {
            model.addAttribute("error", LoginMessage.USERNAME_EMPTY);
            return "login";
        }
        if (StringUtils.isEmpty(password)) {
            model.addAttribute("error", LoginMessage.PASSWORD_EMPTY);
            return "login";
        }

        UserExample example = new UserExample();
        example.createCriteria().andUsernameEqualTo(username);
        List<User> users = userMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(users)) {
            model.addAttribute("error", LoginMessage.USER_NOT_EXIST);
            return "login";
        }
        User user = users.get(0);
        if (BCrypt.checkpw(password, user.getPassword())) {
            // set token
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            userService.updateById(user);
            response.addCookie(new Cookie("token", token));
            return "redirect:/";
        } else {
            model.addAttribute("error", LoginMessage.WRONG_PASSWORD);
            return "login";
        }
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam("username") String username,
                             @RequestParam("name") String name,
                             @RequestParam("avatar") String avatar,
                             @RequestParam("password") String password,
                             @RequestParam("re_password") String rePassword,
                             @RequestParam("bio") String bio,
                             Model model) {
        if (StringUtils.isEmpty(username)) {
            model.addAttribute("error", LoginMessage.USERNAME_EMPTY);
            return "register";
        }
        if (StringUtils.isEmpty(name)) {
            model.addAttribute("error", LoginMessage.NAME_EMPTY);
            return "register";
        }
        if (StringUtils.isEmpty(password)) {
            model.addAttribute("error", LoginMessage.PASSWORD_EMPTY);
            return "register";
        }
        if (StringUtils.isEmpty(rePassword)) {
            model.addAttribute("error", LoginMessage.RE_PASSWORD_EMPTY);
            return "register";
        }
        if (!password.equals(rePassword)) {
            model.addAttribute("error", LoginMessage.PASSWORD_NOT_MATCH);
            return "register";
        }
        if (password.length() > 30) {
            model.addAttribute("error", LoginMessage.PASSWORD_TOO_LONG);
            return "register";
        }

        UserExample example = new UserExample();
        example.createCriteria().andUsernameEqualTo(username);
        List<User> users = userMapper.selectByExample(example);
        if (users != null && users.size() != 0) {
            model.addAttribute("error", LoginMessage.USERNAME_EXISTS);
            return "register";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setName(name);
        user.setGmtCreate(System.currentTimeMillis());
        user.setGmtModified(user.getGmtCreate());
        if (avatar == null || avatar.equals("")) {
            user.setAvatarUrl("/img/anonymous.jpg");
        } else {
            user.setAvatarUrl(avatar);
        }
        user.setBio(bio);
        userMapper.insertSelective(user);
        model.addAttribute("message", "注册成功，请登录");
        model.addAttribute("redirect", "/login");
        return "message";
    }
}
