package com.herokuapp.ddmura.dto;

import com.herokuapp.ddmura.model.Question;
import com.herokuapp.ddmura.model.User;
import lombok.Data;

@Data
public class QuestionDTO extends Question {

    private User user;
    private boolean liked = false;

    public void incLike(int num) {
        this.setLikeCount(this.getLikeCount() + num);
    }

}
