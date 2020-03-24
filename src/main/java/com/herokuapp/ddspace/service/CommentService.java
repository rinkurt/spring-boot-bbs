package com.herokuapp.ddspace.service;

import com.herokuapp.ddspace.cache.AnonymousUser;
import com.herokuapp.ddspace.dto.CommentDTO;
import com.herokuapp.ddspace.enums.CommentType;
import com.herokuapp.ddspace.enums.ResultEnum;
import com.herokuapp.ddspace.mapper.*;
import com.herokuapp.ddspace.model.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@AllArgsConstructor
public class CommentService {

    CommentMapper commentMapper;
    CommentExtMapper commentExtMapper;
    CommentCacheService commentCacheService;
    UserMapper userMapper;
    QuestionMapper questionMapper;
    QuestionExtMapper questionExtMapper;
    LikesMapper likesMapper;
    AnonymousUser anonymousUser;

    LikeService likeService;
    UserService userService;

    @Transactional
    public ResultEnum insert(Comment comment) {
        if (comment.getParentId() == null) {
            return ResultEnum.NULL_PARENT_ID;
        }

        if (comment.getType() == null) {
            return ResultEnum.NULL_COMMENT_TYPE;
        } else if (comment.getType() == CommentType.QUESTION) {
            // 回复问题
            Question parent = questionMapper.selectByPrimaryKey(comment.getParentId());
            if (parent == null) {
                return ResultEnum.QUESTION_NOT_FOUND;
            }
            commentMapper.insertSelective(comment);
            questionExtMapper.incComment(parent.getId(), 1);
        } else if (comment.getType() == CommentType.COMMENT) {
            // 回复评论
            Comment parent = commentMapper.selectByPrimaryKey(comment.getParentId());
            if (parent == null) {
                return ResultEnum.COMMENT_NOT_FOUND;
            }
            commentMapper.insertSelective(comment);
            commentExtMapper.incComment(parent.getId(), 1);
        } else {
            return ResultEnum.NULL_COMMENT_TYPE;
        }

        return ResultEnum.SUCCESS;
    }


    public List<CommentDTO> findByParent(Integer parentId, int type, User sessionUser) {

        List<Comment> comments = commentCacheService.selectByParentAndType(parentId, type);
        HashMap<Integer, User> userMap = new HashMap<>();
        List<CommentDTO> commentDTOList = new ArrayList<>();
        for (Comment comment : comments) {
            Integer userId = comment.getUserId();
            CommentDTO commentDTO = new CommentDTO();
            BeanUtils.copyProperties(comment, commentDTO);
            commentDTO.setLikeCount(Long.valueOf(likeService.getLikeCount(commentDTO.getId(), CommentType.LIKE_COMMENT)));
            // Set user
            if (userMap.containsKey(userId)) {
                commentDTO.setUser(userMap.get(userId));
            } else {
                User user = userService.getById(userId);
                if (user == null) {
                    commentDTO.setUser(anonymousUser);
                } else {
                    userMap.put(userId, user);
                    commentDTO.setUser(user);
                }
            }
            // Set liked
            if (sessionUser != null && commentDTO.getLikeCount() > 0 &&
                    likeService.isContained(commentDTO.getId(), CommentType.LIKE_COMMENT, sessionUser.getId())) {
                commentDTO.setLiked(true);
            }
            commentDTOList.add(commentDTO);
        }
        return commentDTOList;
    }
}
