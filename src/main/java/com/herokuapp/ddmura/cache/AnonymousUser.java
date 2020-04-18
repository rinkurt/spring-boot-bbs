package com.herokuapp.ddspace.cache;

import com.herokuapp.ddspace.model.User;
import org.springframework.stereotype.Component;

@Component
public class AnonymousUser extends User {

    public AnonymousUser() {
        setName("匿名用户");
        setAvatarUrl("/img/anonymous.jpg");
    }

}
