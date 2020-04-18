package com.herokuapp.ddmura.cache;

import com.herokuapp.ddmura.model.User;
import org.springframework.stereotype.Component;

@Component
public class AnonymousUser extends User {

    public AnonymousUser() {
        setName("匿名用户");
        setAvatarUrl("/img/anonymous.jpg");
    }

}
