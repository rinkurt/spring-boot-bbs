package com.herokuapp.ddspace.service;

import com.herokuapp.ddspace.dto.AnonymousUser;
import com.herokuapp.ddspace.dto.CommentType;
import com.herokuapp.ddspace.dto.NotificationDTO;
import com.herokuapp.ddspace.dto.PaginationDTO;
import com.herokuapp.ddspace.enums.ResultEnum;
import com.herokuapp.ddspace.exception.CustomizeException;
import com.herokuapp.ddspace.mapper.CommentMapper;
import com.herokuapp.ddspace.mapper.NotificationMapper;
import com.herokuapp.ddspace.mapper.QuestionMapper;
import com.herokuapp.ddspace.mapper.UserMapper;
import com.herokuapp.ddspace.model.*;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class NotificationService {

    @Autowired(required = false)
    private UserMapper userMapper;

    @Autowired(required = false)
    private QuestionMapper questionMapper;

    @Autowired(required = false)
    private CommentMapper commentMapper;

    @Autowired(required = false)
    private NotificationMapper notificationMapper;

    public ResultEnum insertByComment(Comment comment) {
        Notification notification = new Notification();
        notification.setNotifier(comment.getUserId());
        notification.setOuterid(comment.getParentId());
        notification.setType(comment.getType());
        notification.setGmtCreate(System.currentTimeMillis());
        if (comment.getType() == CommentType.QUESTION) {
            Question question = questionMapper.selectByPrimaryKey(comment.getParentId());
            notification.setReceiver(question.getCreator());
        } else if (comment.getType() == CommentType.COMMENT) {
            Comment parentComment = commentMapper.selectByPrimaryKey(comment.getParentId());
            notification.setReceiver(parentComment.getUserId());
        } else {
            return ResultEnum.NULL_COMMENT_TYPE;
        }
        if (notificationMapper.insertSelective(notification) == 0) {
            return ResultEnum.UNKNOWN;
        }
        return ResultEnum.SUCCESS;
    }

    public ResultEnum insertByLike(Likes like) {
        Notification notification = new Notification();
        notification.setNotifier(like.getUserId());
        notification.setOuterid(like.getParentId());
        notification.setType(like.getType());
        notification.setGmtCreate(like.getGmtCreate());
        if (like.getType() == CommentType.LIKE_QUESTION) {
            Question question = questionMapper.selectByPrimaryKey(like.getParentId());
            notification.setReceiver(question.getCreator());
        } else if (like.getType() == CommentType.LIKE_COMMENT) {
            Comment parentComment = commentMapper.selectByPrimaryKey(like.getParentId());
            notification.setReceiver(parentComment.getUserId());
        } else {
            return ResultEnum.NULL_LIKE_TYPE;
        }
        notificationMapper.insertSelective(notification);
        return ResultEnum.SUCCESS;
    }


    public ResultEnum deleteByLike(Likes like) {
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria()
                .andNotifierEqualTo(like.getUserId())
                .andOuteridEqualTo(like.getParentId())
                .andTypeEqualTo(like.getType());
        notificationMapper.deleteByExample(notificationExample);
        return ResultEnum.SUCCESS;
    }


    public PaginationDTO<NotificationDTO> listByReceiver(Integer receiver, int page, int size) {
        PaginationDTO<NotificationDTO> paginationDTO = new PaginationDTO<>();
        NotificationExample example = new NotificationExample();
        example.createCriteria().andReceiverEqualTo(receiver);
        Integer totalCount = (int) notificationMapper.countByExample(example);
        RowBounds rowBounds = paginationDTO.setPagination(totalCount, page, size);

        example.setOrderByClause("gmt_create desc");
        List<Notification> notifications = notificationMapper.selectByExampleWithRowbounds(example, rowBounds);
        List<NotificationDTO> notificationDTOS = new ArrayList<>();

        for (Notification notification : notifications) {
            NotificationDTO notificationDTO = new NotificationDTO();
            BeanUtils.copyProperties(notification, notificationDTO);
            User user = userMapper.selectByPrimaryKey(notification.getNotifier());
            notificationDTO.setNotifierUser(Objects.requireNonNullElse(user, AnonymousUser.USER));
            int type = notificationDTO.getType();
            if (type == CommentType.QUESTION || type == CommentType.LIKE_QUESTION) {
                Question question = questionMapper.selectByPrimaryKey(notificationDTO.getOuterid());
                notificationDTO.setTitle(question.getTitle());
            } else if (type == CommentType.COMMENT || type == CommentType.LIKE_COMMENT) {
                Comment comment = commentMapper.selectByPrimaryKey(notificationDTO.getOuterid());
                notificationDTO.setTitle(comment.getContent());
            } else {
                throw new CustomizeException(ResultEnum.NULL_COMMENT_TYPE);
            }
            notificationDTOS.add(notificationDTO);
        }
        paginationDTO.setList(notificationDTOS);
        return paginationDTO;
    }

}
