package com.herokuapp.ddmura.mapper;

import com.herokuapp.ddmura.model.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface QuestionExtMapper {
    @Update("update question set view_count = view_count + #{num} where id = #{id}")
    int incView(int id, int num);

    @Update("update question set comment_count = comment_count + #{num} where id = #{id}")
    int incComment(int id, int num);

    @Update("update question set like_count = like_count + #{num} where id = #{id}")
    int incLike(int id, int num);

    @Select("select * from question where id != #{id} and tag ~* #{regexp}")
    List<Question> selectRelated(int id, String regexp);

    @Select("select count(*) from question where title ~* #{search}")
    int countBySearch(String search);

    @Select("select * from question where title ~* #{search} or tag ~* #{search} limit #{limit} offset #{offset}")
    List<Question> selectBySearchWithLimit(String search, int offset, int limit);
}
