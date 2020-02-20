package com.herokuapp.ddspace.dto;

import com.herokuapp.ddspace.model.User;

public class AnonymousUser extends User {

    public static AnonymousUser USER = new AnonymousUser();

    public AnonymousUser() {
        setName("匿名用户");
        setAvatarUrl("/img/anonymous.jpg");
    }

}
