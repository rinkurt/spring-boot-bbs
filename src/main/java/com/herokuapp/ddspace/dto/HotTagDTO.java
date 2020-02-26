package com.herokuapp.ddspace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HotTagDTO {
    private String tag;
    private Integer priority;
    private Integer questionCount = 0;
    private Integer commentCount = 0;

    public HotTagDTO() {}

    public void incQuestion() {
        questionCount += 1;
    }

    public void incComment(int num) {
        commentCount += num;
    }

    public void incPriority(int num) {
        priority += num;
    }
}
