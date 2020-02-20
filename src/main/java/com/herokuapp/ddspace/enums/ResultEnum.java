package com.herokuapp.ddspace.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ResultEnum {

    UNKNOWN(2001, "未知错误"),
    NO_LOGIN(2002, "未登录，请登录后重试"),
    NULL_PARENT_ID(2003, "NullParentId"),
    NULL_COMMENT_TYPE(2004, "NullCommentType"),
    QUESTION_NOT_FOUND(2005, "该问题不存在"),
    COMMENT_NOT_FOUND(2006, "该评论不存在"),
    EMPTY_COMMENT(2007, "评论不能为空"),
    NULL_LIKE_TYPE(2008, "NullLikeType"),
    NOTIFICATION_NOT_FOUND(2009, "通知不存在"),
    REPETITIVE_LOGIN(2010, "不能重复登录"),
    PERMISSION_ERROR(2011, "越权访问"),
    SUCCESS(200, "成功"),
    CLIENT_ERROR(400, "客户端错误"),
    NOT_FOUND(404, "404 NOT FOUND"),
    SERVER_ERROR(500, "服务端错误");

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    Integer code;
    String message;

    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}