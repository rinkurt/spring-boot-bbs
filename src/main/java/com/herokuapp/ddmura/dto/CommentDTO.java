package com.herokuapp.ddmura.dto;

import com.herokuapp.ddmura.model.Comment;
import com.herokuapp.ddmura.model.User;
import lombok.Data;

@Data
public class CommentDTO extends Comment {
    private User user;
    private boolean liked = false;

    public void incLike(int num) {
        this.setLikeCount(this.getLikeCount() + num);
    }
}
