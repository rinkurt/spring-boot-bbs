package com.herokuapp.ddspace.scheduling;

import com.herokuapp.ddspace.mapper.LikesMapper;
import com.herokuapp.ddspace.model.Likes;
import com.herokuapp.ddspace.model.LikesKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
//@Transactional // TODO: redis 事务可用性
public class DataSyncSchedule {

    @Value("${ddspace.cache.db-cache-expire-time}")
    Integer dbCacheExpireTime;

    @Autowired
    RedisTemplate<String, Integer> integerRedisTemplate;

    @Autowired
    LikesMapper likesMapper;

    //@Scheduled(fixedRate = 3600000) // 1 hour
    @Scheduled(fixedRate = 5000)
    public void sync() {
        Set<String> writeKeys = integerRedisTemplate.keys("like:write*");
        if (writeKeys != null && writeKeys.size() > 0) {
            for (String key : writeKeys) {
                String[] split = key.split(":");
                if (split.length < 4) continue;
                Integer type = Integer.valueOf(split[2]);
                Integer parentId = Integer.valueOf(split[3]);
                List<Object> result = integerRedisTemplate.execute(new SessionCallback<List<Object>>() {
                    @Override
                    public List<Object> execute(RedisOperations operations) throws DataAccessException {
                        operations.multi();
                        operations.opsForSet().members(key);
                        operations.delete(key);
                        return operations.exec();
                    }
                });
                // TODO: 加入分布式锁，防止同步期间读操作不一致
                if (result.size() > 0) {
                    Set<Integer> set = (Set<Integer>) result.get(0);
                    if (set != null && set.size() > 0) {
                        // 插入 read 缓存
                        String readKey = key.replace("write", "read");
                        Long expire = integerRedisTemplate.getExpire(readKey);
                        if (expire != null && expire != -2) {
                            Boolean success = true;
                            if (expire > 0) {
                                success = integerRedisTemplate.expire(
                                        readKey, expire + dbCacheExpireTime, TimeUnit.SECONDS);
                            }
                            if (success != null && success) {
                                for (Integer userId : set) {
                                    if (userId > 0) {
                                        integerRedisTemplate.opsForSet().add(readKey, userId);
                                    } else {
                                        integerRedisTemplate.opsForSet().remove(readKey, -userId);
                                    }
                                }
                            }
                        }

                        // 插入数据库
                        for (Integer userId : set) {
                            if (userId > 0) {
                                Likes likes = new Likes();
                                likes.setParentId(parentId);
                                likes.setType(type);
                                likes.setUserId(userId);
                                likes.setGmtCreate(System.currentTimeMillis());
                                likesMapper.insert(likes);
                            } else {
                                LikesKey likesKey = new LikesKey();
                                likesKey.setType(type);
                                likesKey.setParentId(parentId);
                                likesKey.setUserId(-userId);
                                likesMapper.deleteByPrimaryKey(likesKey);
                            }
                        }
                    }
                }
            }
        }
    }
}
