package com.herokuapp.ddspace.service;

import com.herokuapp.ddspace.dto.AnonymousUser;
import com.herokuapp.ddspace.dto.CommentType;
import com.herokuapp.ddspace.dto.PaginationDTO;
import com.herokuapp.ddspace.dto.QuestionDTO;
import com.herokuapp.ddspace.exception.CustomizeException;
import com.herokuapp.ddspace.enums.ResultEnum;
import com.herokuapp.ddspace.mapper.LikesMapper;
import com.herokuapp.ddspace.mapper.QuestionExtMapper;
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
public class QuestionService {

    @Autowired(required = false)
    private QuestionMapper questionMapper;

    @Autowired(required = false)
    private QuestionExtMapper questionExtMapper;

    @Autowired(required = false)
    private UserMapper userMapper;

    @Autowired(required = false)
    private LikesMapper likesMapper;


    public PaginationDTO<QuestionDTO> listByExample(QuestionExample example, String search, Integer page, Integer size) {
        PaginationDTO<QuestionDTO> paginationDTO = new PaginationDTO<>();
        int totalCount;
        if (search != null) {
            StringBuilder sb = new StringBuilder();
            for (String s : search.split(" ")) {
                if (s != null && !s.equals("")) {
                    if (sb.length() != 0) {
                        sb.append('|');
                    }
                    sb.append(s);
                }
            }
            search = sb.toString();
            search = search.toLowerCase();
            totalCount = questionExtMapper.countBySearch(search);
        } else {
            totalCount = (int) questionMapper.countByExample(example);
        }
        RowBounds rowBounds = paginationDTO.setPagination(totalCount, page, size);

        example.setOrderByClause("gmt_modified desc");
        List<Question> questions;
        if (search != null) {
            questions = questionExtMapper.selectBySearchWithLimit(search, rowBounds.getOffset(), rowBounds.getLimit());
        } else {
            questions = questionMapper.selectByExampleWithRowbounds(example, rowBounds);
        }
        List<QuestionDTO> questionDTOList = new ArrayList<>();
        for (Question question : questions) {
            QuestionDTO questionDto = new QuestionDTO();
            BeanUtils.copyProperties(question, questionDto);
            User user = userMapper.selectByPrimaryKey(question.getCreator());
            if (user != null) {
                questionDto.setUser(user);
            } else {
                questionDto.setUser(AnonymousUser.USER);
            }
            questionDTOList.add(questionDto);
        }
        paginationDTO.setList(questionDTOList);

        return paginationDTO;
    }

    public PaginationDTO<QuestionDTO> listByExample(QuestionExample example, Integer page, Integer size) {
        return listByExample(example, null, page, size);
    }

    public PaginationDTO<QuestionDTO> list(String search, Integer page, Integer size) {
        return listByExample(new QuestionExample(), search, page, size);
    }

    public PaginationDTO<QuestionDTO> listByUser(Integer userId, Integer page, Integer size) {
        QuestionExample example = new QuestionExample();
        example.createCriteria().andCreatorEqualTo(userId);
        return listByExample(example, page, size);
    }

    public QuestionDTO findById(Integer id, User sessionUser) {
        Question question = questionMapper.selectByPrimaryKey(id);
        if (question == null) {
            throw new CustomizeException(ResultEnum.QUESTION_NOT_FOUND);
        }
        QuestionDTO questionDto = new QuestionDTO();
        BeanUtils.copyProperties(question, questionDto);
        questionDto.setUser(userMapper.selectByPrimaryKey(questionDto.getCreator()));
        if (sessionUser != null) {
            LikesKey likesKey = new LikesKey();
            likesKey.setType(CommentType.LIKE_QUESTION);
            likesKey.setParentId(id);
            likesKey.setUserId(sessionUser.getId());
            Likes likes = likesMapper.selectByPrimaryKey(likesKey);
            if (likes != null) {
                questionDto.setLiked(true);
            }
        }
        return questionDto;
    }

    public void incView(int id, int num) {
        if (questionExtMapper.incView(id, num) == 0) {
            throw new CustomizeException(ResultEnum.QUESTION_NOT_FOUND);
        }
    }

    public void createOrUpdate(Question question) {
        if (question.getId() == null) {
            // 新建
            question.setGmtCreate(System.currentTimeMillis());
            question.setGmtModified(question.getGmtCreate());
            questionMapper.insertSelective(question);
        } else {
            // 更新
            question.setGmtCreate(null);    // 不更新创建时间
            question.setGmtModified(System.currentTimeMillis());
            question.setCreator(null);      // 不更新创建者
            if (questionMapper.updateByPrimaryKeySelective(question) == 0) {
                throw new CustomizeException(ResultEnum.QUESTION_NOT_FOUND);
            }
        }
    }
}
