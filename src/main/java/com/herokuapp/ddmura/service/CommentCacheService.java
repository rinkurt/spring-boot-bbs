package com.herokuapp.ddmura.service;

import com.herokuapp.ddmura.mapper.CommentMapper;
import com.herokuapp.ddmura.model.Comment;
import com.herokuapp.ddmura.model.CommentExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentCacheService {

    @Autowired
    private CommentMapper commentMapper;

    //@Cacheable(value = {"comment"}, key = "#parentId + '-' + #type")
    public List<Comment> selectByParentAndType(Integer parentId, Integer type) {
        CommentExample commentExample = new CommentExample();
        commentExample.createCriteria()
                .andParentIdEqualTo(parentId)
                .andTypeEqualTo(type);
        commentExample.setOrderByClause("gmt_create desc");
        return commentMapper.selectByExample(commentExample);
    }

}
