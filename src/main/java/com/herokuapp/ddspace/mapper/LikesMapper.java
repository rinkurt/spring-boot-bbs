package com.herokuapp.ddspace.mapper;

import com.herokuapp.ddspace.model.Likes;
import com.herokuapp.ddspace.model.LikesExample;
import com.herokuapp.ddspace.model.LikesKey;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface LikesMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LIKES
     *
     * @mbg.generated Wed Feb 19 19:03:29 CST 2020
     */
    long countByExample(LikesExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LIKES
     *
     * @mbg.generated Wed Feb 19 19:03:29 CST 2020
     */
    int deleteByExample(LikesExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LIKES
     *
     * @mbg.generated Wed Feb 19 19:03:29 CST 2020
     */
    int deleteByPrimaryKey(LikesKey key);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LIKES
     *
     * @mbg.generated Wed Feb 19 19:03:29 CST 2020
     */
    int insert(Likes record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LIKES
     *
     * @mbg.generated Wed Feb 19 19:03:29 CST 2020
     */
    int insertSelective(Likes record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LIKES
     *
     * @mbg.generated Wed Feb 19 19:03:29 CST 2020
     */
    List<Likes> selectByExampleWithRowbounds(LikesExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LIKES
     *
     * @mbg.generated Wed Feb 19 19:03:29 CST 2020
     */
    List<Likes> selectByExample(LikesExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LIKES
     *
     * @mbg.generated Wed Feb 19 19:03:29 CST 2020
     */
    Likes selectByPrimaryKey(LikesKey key);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LIKES
     *
     * @mbg.generated Wed Feb 19 19:03:29 CST 2020
     */
    int updateByExampleSelective(@Param("record") Likes record, @Param("example") LikesExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LIKES
     *
     * @mbg.generated Wed Feb 19 19:03:29 CST 2020
     */
    int updateByExample(@Param("record") Likes record, @Param("example") LikesExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LIKES
     *
     * @mbg.generated Wed Feb 19 19:03:29 CST 2020
     */
    int updateByPrimaryKeySelective(Likes record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table LIKES
     *
     * @mbg.generated Wed Feb 19 19:03:29 CST 2020
     */
    int updateByPrimaryKey(Likes record);
}