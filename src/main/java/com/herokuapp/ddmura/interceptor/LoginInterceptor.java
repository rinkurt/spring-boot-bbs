package com.herokuapp.ddspace.interceptor;

import com.herokuapp.ddspace.mapper.NotificationMapper;
import com.herokuapp.ddspace.mapper.UserMapper;
import com.herokuapp.ddspace.model.NotificationExample;
import com.herokuapp.ddspace.model.User;
import com.herokuapp.ddspace.model.UserExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Service
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired(required = false)
    UserMapper userMapper;

    @Autowired(required = false)
    NotificationMapper notificationMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token")) {
                    String token = cookie.getValue();
                    UserExample userExample = new UserExample();
                    userExample.createCriteria().andTokenEqualTo(token);
                    List<User> users = userMapper.selectByExample(userExample);
                    if (users.size() > 0) {
                        User user = users.get(0);
                        request.getSession().setAttribute("user", user);
                        NotificationExample example = new NotificationExample();
                        example.createCriteria()
                                .andReceiverEqualTo(user.getId())
                                .andStatusEqualTo(0);
                        long notificationCount = notificationMapper.countByExample(example);
                        request.getSession().setAttribute("notificationCount", notificationCount);
                    }
                    break;
                }
            }
        }
        // System.out.println("preHandle");
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
