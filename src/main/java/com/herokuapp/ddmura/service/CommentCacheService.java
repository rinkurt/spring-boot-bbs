package com.herokuapp.ddspace.service;

import com.herokuapp.ddspace.mapper.CommentMapper;
import com.herokuapp.ddspace.model.Comment;
import com.herokuapp.ddspace.model.CommentExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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
