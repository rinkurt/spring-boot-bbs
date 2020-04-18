package com.herokuapp.ddspace.dto;

import com.herokuapp.ddspace.model.Comment;
import com.herokuapp.ddspace.model.User;
import lombok.Data;

@Data
public class CommentDTO extends Comment {
    private User user;
    private boolean liked = false;

    public void incLike(int num) {
        this.setLikeCount(this.getLikeCount() + num);
    }
}
