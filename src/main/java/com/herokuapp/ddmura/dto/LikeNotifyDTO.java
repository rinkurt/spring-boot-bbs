package com.herokuapp.ddmura.dto;

import com.herokuapp.ddmura.model.User;
import lombok.Data;

import java.util.List;

@Data
public class LikeNotifyDTO {
    Integer parentId;
    Integer type;
    String content;
    List<User> senders;
    Integer count;
}
