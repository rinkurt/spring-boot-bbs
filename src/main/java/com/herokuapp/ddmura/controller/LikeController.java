package com.herokuapp.ddmura.controller;

import com.herokuapp.ddmura.enums.CommentType;
import com.herokuapp.ddmura.dto.LikeDTO;
import com.herokuapp.ddmura.enums.ResultEnum;
import com.herokuapp.ddmura.mapper.*;
import com.herokuapp.ddmura.model.*;
import com.herokuapp.ddmura.service.LikeService;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@AllArgsConstructor
public class LikeController {
    
    private LikeService likeService;

    @ResponseBody
    @PostMapping("/like")
    public Object like(@RequestBody LikeDTO likeDTO, HttpServletRequest request) {

        Object obj = request.getSession().getAttribute("user");
        if (!(obj instanceof User)) {
            return ResultEnum.NO_LOGIN;
        }
        User user = (User) obj;
        if (likeDTO == null || likeDTO.getId() == null) {
            return ResultEnum.CLIENT_ERROR;
        }
        if (likeDTO.getType() != CommentType.LIKE_QUESTION &&
                likeDTO.getType() != CommentType.LIKE_COMMENT) {
            return ResultEnum.NULL_LIKE_TYPE;
        }

        // 不能给自己点赞
        if (likeDTO.getReceiveId().equals(user.getId())) {
            return ResultEnum.FAIL;
        }

        LikesKey likesKey = new LikesKey();
        likesKey.setUserId(user.getId());
        likesKey.setParentId(likeDTO.getId());
        likesKey.setType(likeDTO.getType());

        Likes like = new Likes();
        like.setGmtCreate(System.currentTimeMillis());
        BeanUtils.copyProperties(likesKey, like);

        if (!likeDTO.getLiked()) {
            // 点赞
            likeService.like(likeDTO.getId(), likeDTO.getType(), user.getId());
        } else {
            // 取消
            likeService.like(likeDTO.getId(), likeDTO.getType(), -user.getId());
        }

        if (!likeDTO.getLiked()) {
            return ResultEnum.SUCCESS;
        } else {
            return ResultEnum.CANCELED;
        }

    }
}
