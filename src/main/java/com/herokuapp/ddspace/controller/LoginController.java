package com.herokuapp.ddspace.controller;

import com.herokuapp.ddspace.enums.ResultEnum;
import com.herokuapp.ddspace.exception.CustomizeException;
import com.herokuapp.ddspace.mapper.UserMapper;
import com.herokuapp.ddspace.model.User;
import com.herokuapp.ddspace.model.UserExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    @Value("${github.redirect_uri}")
    private String redirectUri;

    @Autowired(required = false)
    private UserMapper userMapper;

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        User user = (User) request.getSession().getAttribute("user");
        if (user != null) {
            throw new CustomizeException(ResultEnum.REPETITIVE_LOGIN);
        }
        model.addAttribute("clientId", System.getenv("GITHUB_CLIENT_ID"));
        model.addAttribute("redirectUri", redirectUri);
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          Model model,
                          HttpServletResponse response) {
        if (username == null || username.equals("")) {
            model.addAttribute("error", "邮箱不能为空");
            return "login";
        }
        if (password == null || password.equals("")) {
            model.addAttribute("error", "密码不能为空");
            return "login";
        }

        UserExample example = new UserExample();
        example.createCriteria().andUsernameEqualTo(username);
        List<User> users = userMapper.selectByExample(example);
        if (users == null || users.size() == 0) {
            model.addAttribute("error", "用户不存在");
            return "login";
        }
        User user = users.get(0);
        if (user.getPassword().equals(password)) {
            // set token
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            user.setGmtModified(System.currentTimeMillis());
            userMapper.updateByPrimaryKeySelective(user);
            response.addCookie(new Cookie("token", token));
            return "redirect:/";
        } else {
            model.addAttribute("error", "密码错误");
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
        if (username == null || username.equals("")) {
            model.addAttribute("error", "邮箱不能为空");
            return "register";
        }
        if (name == null || name.equals("")) {
            model.addAttribute("error", "名字不能为空");
            return "register";
        }
        if (password == null || password.equals("")) {
            model.addAttribute("error", "密码不能为空");
            return "register";
        }
        if (rePassword == null || rePassword.equals("")) {
            model.addAttribute("error", "确认密码不能为空");
            return "register";
        }
        if (!password.equals(rePassword)) {
            model.addAttribute("error", "密码不一致");
            return "register";
        }

        UserExample example = new UserExample();
        example.createCriteria().andUsernameEqualTo(username);
        List<User> users = userMapper.selectByExample(example);
        if (users != null && users.size() != 0) {
            model.addAttribute("error", "邮箱已存在");
            return "register";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
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
