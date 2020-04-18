package com.herokuapp.ddspace.dto;

import com.herokuapp.ddspace.model.User;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class LikeNotifyDTO {
    Integer parentId;
    Integer type;
    String content;
    List<User> senders;
    Integer count;
}
