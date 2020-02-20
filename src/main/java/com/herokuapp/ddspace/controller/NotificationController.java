package com.herokuapp.ddspace.controller;

import com.herokuapp.ddspace.dto.CommentType;
import com.herokuapp.ddspace.enums.ResultEnum;
import com.herokuapp.ddspace.exception.CustomizeException;
import com.herokuapp.ddspace.mapper.CommentMapper;
import com.herokuapp.ddspace.mapper.NotificationMapper;
import com.herokuapp.ddspace.model.Comment;
import com.herokuapp.ddspace.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;

@Controller
public class NotificationController {

    @Autowired(required = false)
    CommentMapper commentMapper;

    @Autowired(required = false)
    NotificationMapper notificationMapper;

    @GetMapping("/notification/{id}")
    public String readNotification(@PathVariable("id") Integer id, HttpServletRequest request) {
        Notification dbNotification = notificationMapper.selectByPrimaryKey(id);
        if (dbNotification == null) {
            throw new CustomizeException(ResultEnum.NOT_FOUND);
        }
        if (dbNotification.getStatus() == 0) {
            Notification notification = new Notification();
            notification.setId(id);
            notification.setStatus(1);
            if (notificationMapper.updateByPrimaryKeySelective(notification) == 0) {
                throw new CustomizeException(ResultEnum.NOTIFICATION_NOT_FOUND);
            }
            Long count = (Long) request.getSession().getAttribute("notificationCount");
            request.getSession().setAttribute("notificationCount", count - 1);
        }
        if (dbNotification.getType() == CommentType.QUESTION ||
                dbNotification.getType() == CommentType.LIKE_QUESTION) {
            return "redirect:/question/" + dbNotification.getOuterid();
        } else if (dbNotification.getType() == CommentType.COMMENT ||
                dbNotification.getType() == CommentType.LIKE_COMMENT) {
            Comment comment = commentMapper.selectByPrimaryKey(dbNotification.getOuterid());
            return "redirect:/question/" + comment.getParentId() + "#comment_" + comment.getId();
        } else {
            throw new CustomizeException(ResultEnum.NULL_COMMENT_TYPE);
        }
    }
}
