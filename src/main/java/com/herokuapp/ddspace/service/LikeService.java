package com.herokuapp.ddspace.service;

import com.herokuapp.ddspace.dto.CommentType;
import com.herokuapp.ddspace.enums.ResultEnum;
import com.herokuapp.ddspace.mapper.LikesMapper;
import com.herokuapp.ddspace.model.Likes;
import com.herokuapp.ddspace.model.LikesExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class LikeService {

    @Value("${ddspace.cache.read-expire-time}")
    private Integer expireTime;

    @Value("${ddspace.cache.empty-expire-time}")
    private Integer emptyExpireTime;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Integer> integerRedisTemplate;

    @Autowired
    private LikesMapper likesMapper;


    // 点赞，传入负的 userId 表示取消
    public ResultEnum like(Integer parentId, Integer type, Integer userId) {
        integerRedisTemplate.setEnableTransactionSupport(true);
        integerRedisTemplate.multi();
        integerRedisTemplate.opsForSet().remove("like:write:" + type + ":" + parentId, -userId);
        integerRedisTemplate.opsForSet().add("like:write:" + type + ":" + parentId, userId);
        if (userId > 0) {
            integerRedisTemplate.opsForValue().increment("like-count:" + type + ":" + parentId);
        } else {
            integerRedisTemplate.opsForValue().decrement("like-count:" + type + ":" + parentId);
        }
        integerRedisTemplate.exec();
        return ResultEnum.SUCCESS;
    }



    public Set<Integer> getLikeSet(Integer parentId, Integer type) {
        Set<Integer> writeSet = integerRedisTemplate.opsForSet().members("like:write:" + type + ":" + parentId);
        Set<Integer> readSet = integerRedisTemplate.opsForSet().members("like:read:" + type + ":" + parentId);
        if (readSet == null || readSet.size() == 0) {
            // read dataset
            LikesExample likesExample = new LikesExample();
            likesExample.createCriteria()
                    .andParentIdEqualTo(parentId)
                    .andTypeEqualTo(type);
            List<Likes> likesList = likesMapper.selectByExample(likesExample);
            if (likesList.size() > 0) {
                Integer[] values = new Integer[likesList.size()];
                int i = 0;
                for (Likes likes : likesList) {
                    values[i] = likes.getUserId();
                    i++;
                }
                integerRedisTemplate.opsForSet().add("like:read:" + type + ":" + parentId, values);
                integerRedisTemplate.expire("like:read:" + type + ":" + parentId, expireTime, TimeUnit.SECONDS);
                readSet = new HashSet<>(Arrays.asList(values));
            } else {
                readSet = new HashSet<>();
                // 向缓存插入 0 用于占位，防止重复读库
                integerRedisTemplate.opsForSet().add("like:read:" + type + ":" + parentId, 0);
                integerRedisTemplate.expire("like:read:" + type + ":" + parentId, emptyExpireTime, TimeUnit.SECONDS);
            }
        }
        if (writeSet != null) {
            for (Integer id : writeSet) {
                if (id < 0) {
                    readSet.remove(-id);
                } else {
                    readSet.add(id);
                }
            }
        }
        readSet.remove(0);
        return readSet;
    }


    public boolean isContained(Integer parentId, Integer type, Integer userId) {
        Set<Integer> likeSet = getLikeSet(parentId, type);
        return likeSet.contains(userId);
    }


    public Integer getLikeCountFromRedis(Integer parentId, Integer type) {
        Integer result = integerRedisTemplate.opsForValue().get("like-count:" + type + ":" + parentId);
        if (result == null) return 0;
        else return result;
    }

}
