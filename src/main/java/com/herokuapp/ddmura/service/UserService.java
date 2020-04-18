package com.herokuapp.ddmura.service;

import com.herokuapp.ddmura.mapper.UserMapper;
import com.herokuapp.ddmura.model.User;
import com.herokuapp.ddmura.model.UserExample;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private UserMapper userMapper;

    public void createOrUpdate(User user) {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andAccountIdEqualTo(user.getAccountId());
        List<User> users = userMapper.selectByExample(userExample);
        if (users.size() == 0) {
            // 新建
            user.setGmtCreate(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreate());
            userMapper.insertSelective(user);
        } else {
            // 更新
            User dbUser = users.get(0);
            user.setGmtModified(System.currentTimeMillis());
            user.setId(dbUser.getId());     // id 传入数据库
            userMapper.updateByPrimaryKeySelective(user);
        }
    }
}
