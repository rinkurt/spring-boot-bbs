package com.herokuapp.ddspace.dto;

import com.herokuapp.ddspace.model.Question;
import com.herokuapp.ddspace.model.User;
import lombok.Data;

@Data
public class QuestionDTO extends Question {

    private User user;
    private boolean liked = false;

    public void incLike(int num) {
        this.setLikeCount(this.getLikeCount() + num);
    }

}
