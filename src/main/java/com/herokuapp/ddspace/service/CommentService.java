package com.herokuapp.ddspace.service;

import com.herokuapp.ddspace.dto.AnonymousUser;
import com.herokuapp.ddspace.dto.CommentDTO;
import com.herokuapp.ddspace.dto.CommentType;
import com.herokuapp.ddspace.enums.ResultEnum;
import com.herokuapp.ddspace.mapper.*;
import com.herokuapp.ddspace.model.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class CommentService {

    @Autowired(required = false)
    CommentMapper commentMapper;

    @Autowired(required = false)
    CommentExtMapper commentExtMapper;

    @Autowired(required = false)
    UserMapper userMapper;

    @Autowired(required = false)
    QuestionMapper questionMapper;

    @Autowired(required = false)
    QuestionExtMapper questionExtMapper;

    @Autowired(required = false)
    LikesMapper likesMapper;

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
        CommentExample commentExample = new CommentExample();
        commentExample.createCriteria()
                .andParentIdEqualTo(parentId)
                .andTypeEqualTo(type);
        commentExample.setOrderByClause("gmt_create desc");
        List<Comment> comments = commentMapper.selectByExample(commentExample);
        HashMap<Integer, User> userMap = new HashMap<>();
        List<CommentDTO> commentDTOList = new ArrayList<>();
        for (Comment comment : comments) {
            Integer userId = comment.getUserId();
            CommentDTO commentDTO = new CommentDTO();
            BeanUtils.copyProperties(comment, commentDTO);
            // Set user
            if (userMap.containsKey(userId)) {
                commentDTO.setUser(userMap.get(userId));
            } else {
                User user = userMapper.selectByPrimaryKey(userId);
                if (user == null) {
                    commentDTO.setUser(AnonymousUser.USER);
                } else {
                    userMap.put(userId, user);
                    commentDTO.setUser(user);
                }
            }
            // Set liked
            if (sessionUser != null) {
                LikesKey likesKey = new LikesKey();
                likesKey.setType(CommentType.LIKE_COMMENT);
                likesKey.setParentId(comment.getId());
                likesKey.setUserId(sessionUser.getId());
                Likes likes = likesMapper.selectByPrimaryKey(likesKey);
                if (likes != null) {
                    commentDTO.setLiked(true);
                }
            }
            commentDTOList.add(commentDTO);
        }
        return commentDTOList;
    }
}
