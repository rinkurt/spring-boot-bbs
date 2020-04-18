package com.herokuapp.ddmura.controller;

import com.herokuapp.ddmura.enums.CommentType;
import com.herokuapp.ddmura.enums.ResultEnum;
import com.herokuapp.ddmura.exception.CustomizeException;
import com.herokuapp.ddmura.mapper.CommentMapper;
import com.herokuapp.ddmura.mapper.NotificationMapper;
import com.herokuapp.ddmura.model.Comment;
import com.herokuapp.ddmura.model.Notification;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;

@Controller
@AllArgsConstructor
public class NotificationController {

    CommentMapper commentMapper;
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
