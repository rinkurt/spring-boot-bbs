package com.herokuapp.ddmura.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CommentExtMapper {
    @Update("update comment set like_count = like_count + #{num} where id = #{id}")
    int incLike(int id, int num);

    @Update("update comment set comment_count = comment_count + #{num} where id = #{id}")
    int incComment(int id, int num);
}
