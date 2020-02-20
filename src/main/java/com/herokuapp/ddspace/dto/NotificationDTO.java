package com.herokuapp.ddspace.dto;

import com.herokuapp.ddspace.model.Notification;
import com.herokuapp.ddspace.model.User;
import lombok.Data;

@Data
public class NotificationDTO extends Notification {
    private User notifierUser;
    private String title;
}
