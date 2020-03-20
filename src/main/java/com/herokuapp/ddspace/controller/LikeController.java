package com.herokuapp.ddspace.controller;

import com.herokuapp.ddspace.dto.CommentType;
import com.herokuapp.ddspace.dto.LikeDTO;
import com.herokuapp.ddspace.enums.ResultEnum;
import com.herokuapp.ddspace.mapper.*;
import com.herokuapp.ddspace.model.*;
import com.herokuapp.ddspace.service.LikeService;
import com.herokuapp.ddspace.service.NotificationService;
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

    private QuestionExtMapper questionExtMapper;
    private CommentExtMapper commentExtMapper;
    private LikesMapper likesMapper;

    private LikeService likeService;

    @ResponseBody
    @PostMapping("/like")
    public Object like(@RequestBody LikeDTO likeDTO, HttpServletRequest request) {

        boolean cancel = false;

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return ResultEnum.NO_LOGIN;
        }
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

        int num;

        Likes like = new Likes();
        like.setGmtCreate(System.currentTimeMillis());
        BeanUtils.copyProperties(likesKey, like);

        if (!likeDTO.getLiked()) {
            // 点赞
            //likesMapper.insertSelective(like);
            likeService.like(likeDTO.getId(), likeDTO.getType(), user.getId());
            num = 1;
        } else {
            // 取消
            //likesMapper.deleteByPrimaryKey(likesKey);
            likeService.like(likeDTO.getId(), likeDTO.getType(), -user.getId());
            num = -1;
        }


        if (!likeDTO.getLiked()) {
            return ResultEnum.SUCCESS;
        } else {
            return ResultEnum.CANCELED;
        }

    }
}
