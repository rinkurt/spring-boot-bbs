package com.herokuapp.ddspace.controller;

import com.herokuapp.ddspace.dto.CommentType;
import com.herokuapp.ddspace.dto.LikeDTO;
import com.herokuapp.ddspace.enums.ResultEnum;
import com.herokuapp.ddspace.mapper.*;
import com.herokuapp.ddspace.model.*;
import com.herokuapp.ddspace.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@AllArgsConstructor
public class LikeController {

    private NotificationService notificationService;
    private QuestionExtMapper questionExtMapper;
    private CommentExtMapper commentExtMapper;
    private QuestionMapper questionMapper;
    private CommentMapper commentMapper;
    private LikesMapper likesMapper;

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
        if (likeDTO.getType() == CommentType.LIKE_QUESTION) {
            Question question = questionMapper.selectByPrimaryKey(likeDTO.getId());
            if (question.getCreator().equals(user.getId())) {
                return ResultEnum.SUCCESS;
            }
        } else if (likeDTO.getType() == CommentType.LIKE_COMMENT) {
            Comment comment = commentMapper.selectByPrimaryKey(likeDTO.getId());
            if (comment.getUserId().equals(user.getId())) {
                return ResultEnum.SUCCESS;
            }
        } else {
            return ResultEnum.NULL_COMMENT_TYPE;
        }

        LikesKey likesKey = new LikesKey();
        likesKey.setUserId(user.getId());
        likesKey.setParentId(likeDTO.getId());
        likesKey.setType(likeDTO.getType());
        Likes dbLike = likesMapper.selectByPrimaryKey(likesKey);

        int num;

        Likes like = new Likes();
        like.setGmtCreate(System.currentTimeMillis());
        BeanUtils.copyProperties(likesKey, like);

        if (dbLike == null) {
            // 点赞
            likesMapper.insertSelective(like);
            num = 1;
        } else {
            // 取消
            likesMapper.deleteByPrimaryKey(likesKey);
            num = -1;
            cancel = true;
        }

        likeDTO.setUserId(user.getId());
        if (likeDTO.getType() == CommentType.LIKE_QUESTION) {
            // 给问题点赞
            if (questionExtMapper.incLike(likeDTO.getId(), num) == 0) {
                return ResultEnum.QUESTION_NOT_FOUND;
            }
        } else if (likeDTO.getType() == CommentType.LIKE_COMMENT) {
            // 给评论点赞
            if (commentExtMapper.incLike(likeDTO.getId(), num) == 0) {
                return ResultEnum.COMMENT_NOT_FOUND;
            }
        }

        // Notification
        if (!cancel) {
            return notificationService.insertByLike(like);
        } else {
            return notificationService.deleteByLike(like);
        }
    }
}
