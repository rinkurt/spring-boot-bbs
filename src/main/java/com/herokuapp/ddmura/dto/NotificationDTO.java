package com.herokuapp.ddmura.dto;

import com.herokuapp.ddmura.model.Notification;
import com.herokuapp.ddmura.model.User;
import lombok.Data;

@Data
public class NotificationDTO extends Notification {
    private User notifierUser;
    private String title;
}
