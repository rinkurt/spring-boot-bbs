package com.herokuapp.ddmura.service;

import com.herokuapp.ddmura.cache.AnonymousUser;
import com.herokuapp.ddmura.dto.CommentType;
import com.herokuapp.ddmura.dto.NotificationDTO;
import com.herokuapp.ddmura.dto.PaginationDTO;
import com.herokuapp.ddmura.enums.ResultEnum;
import com.herokuapp.ddmura.exception.CustomizeException;
import com.herokuapp.ddmura.mapper.CommentMapper;
import com.herokuapp.ddmura.mapper.NotificationMapper;
import com.herokuapp.ddmura.mapper.QuestionMapper;
import com.herokuapp.ddmura.mapper.UserMapper;
import com.herokuapp.ddmura.model.*;
import lombok.AllArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class NotificationService {

    private UserMapper userMapper;
    private QuestionMapper questionMapper;
    private CommentMapper commentMapper;
    private NotificationMapper notificationMapper;
    private AnonymousUser anonymousUser;

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
        if (notification.getReceiver().equals(notification.getNotifier())) {
            return ResultEnum.SUCCESS;
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
            if (user != null) {
                notificationDTO.setNotifierUser(user);
            } else {
                notificationDTO.setNotifierUser(anonymousUser);
            }
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
