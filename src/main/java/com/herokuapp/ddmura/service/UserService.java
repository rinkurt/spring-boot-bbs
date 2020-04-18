package com.herokuapp.ddmura.service;

import com.herokuapp.ddmura.mapper.UserMapper;
import com.herokuapp.ddmura.model.User;
import com.herokuapp.ddmura.model.UserExample;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private UserMapper userMapper;

    @Cacheable(value = "user", key = "#id")
    public User getById(Integer id) {
        return userMapper.selectByPrimaryKey(id);
    }

    @CachePut(value = "user", key = "#result.id", condition = "#result.id != null")
    public User createOrUpdateByAccountId(User user) {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andAccountIdEqualTo(user.getAccountId());
        List<User> users = userMapper.selectByExample(userExample);
        if (users.size() == 0) {
            // 新建
            user.setGmtCreate(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreate());
            userMapper.insertSelective(user);
            List<User> readUsers = userMapper.selectByExample(userExample);
            if (readUsers.size() != 0) {
                user.setId(readUsers.get(0).getId());
            }
        } else {
            // 更新
            User dbUser = users.get(0);
            user.setGmtModified(System.currentTimeMillis());
            user.setId(dbUser.getId());
            userMapper.updateByPrimaryKeySelective(user);
        }
        return user;
    }

    @CachePut(value = "user", key = "#result.id", condition = "#result.id != null")
    public User updateById(User user) {
        user.setGmtModified(System.currentTimeMillis());
        userMapper.updateByPrimaryKeySelective(user);
        return user;
    }
}
