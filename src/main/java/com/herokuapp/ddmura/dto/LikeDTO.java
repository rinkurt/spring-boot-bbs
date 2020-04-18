package com.herokuapp.ddspace.dto;

import lombok.Data;

@Data
public class LikeDTO {
    private Integer id;
    private int type;
    private Integer receiveId;
    private Boolean liked;
}
