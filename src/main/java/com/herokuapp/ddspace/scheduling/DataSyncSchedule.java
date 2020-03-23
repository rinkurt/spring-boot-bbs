package com.herokuapp.ddspace.scheduling;

import com.herokuapp.ddspace.MyUtils;
import com.herokuapp.ddspace.dto.CommentType;
import com.herokuapp.ddspace.mapper.CommentMapper;
import com.herokuapp.ddspace.mapper.LikesMapper;
import com.herokuapp.ddspace.mapper.QuestionMapper;
import com.herokuapp.ddspace.model.Comment;
import com.herokuapp.ddspace.model.Likes;
import com.herokuapp.ddspace.model.LikesKey;
import com.herokuapp.ddspace.model.Question;
import com.herokuapp.ddspace.service.LikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class DataSyncSchedule {

    @Value("${ddspace.cache.db-cache-expire-time}")
    Integer dbCacheExpireTime;

    @Value("${ddspace.cache.read-expire-time}")
    Integer expireTime;

    @Autowired
    RedisTemplate<String, Integer> integerRedisTemplate;

    @Autowired
    LikesMapper likesMapper;

    @Autowired
    QuestionMapper questionMapper;

    @Autowired
    CommentMapper commentMapper;

    @Autowired
    LikeService likeService;

    //@Scheduled(fixedRate = 3600000) // 1 hour
    @Scheduled(fixedRate = 60000)
    public void sync() {
        Set<String> writeKeys = integerRedisTemplate.keys("like:write*");
        if (writeKeys != null && writeKeys.size() > 0) {
            for (String key : writeKeys) {
                String[] split = key.split(":");
                if (split.length < 4) continue;
                Integer type = MyUtils.parseInt(split[2]);
                Integer parentId = MyUtils.parseInt(split[3]);
                // 先重命名key，防止之后同步时，用户插入操作造成不一致
                String dbKey = key.replace("write", "db");
                Boolean hasKey = integerRedisTemplate.hasKey(dbKey);
                if (hasKey != null && !hasKey) {
                    integerRedisTemplate.rename(key, dbKey);
                }
                // TODO: 加入分布式锁，防止同步期间读操作不一致
                Set<Integer> set = integerRedisTemplate.opsForSet().members(dbKey);
                if (set != null && set.size() > 0) {
                    // 写入读缓存中
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

                    // 写入数据库中
                    for (Integer userId : set) {
                        if (userId > 0) {
                            Likes likes = new Likes();
                            likes.setParentId(parentId);
                            likes.setType(type);
                            likes.setUserId(userId);
                            likes.setGmtCreate(System.currentTimeMillis());
                            try {
                                likesMapper.insertSelective(likes);
                            } catch (DuplicateKeyException e) {
                                log.error("Duplicate key: (" + parentId + ", " + type + ", " + userId + ")");
                            }
                        } else {
                            LikesKey likesKey = new LikesKey();
                            likesKey.setType(type);
                            likesKey.setParentId(parentId);
                            likesKey.setUserId(-userId);
                            likesMapper.deleteByPrimaryKey(likesKey);
                        }
                    }
                }
                integerRedisTemplate.delete(dbKey);
            }
        }
        Set<String> countKeys = integerRedisTemplate.keys("like-count:*");
        if (countKeys != null && countKeys.size() > 0) {
            for (String key : countKeys) {
                Long expire = integerRedisTemplate.getExpire(key);
                // 仅将无过期时间的key写入数据库
                if (expire != null && expire == -1) {
                    Integer likeCount = integerRedisTemplate.opsForValue().get(key);
                    if (likeCount != null) {
                        String[] split = key.split(":");
                        if (split.length < 3) continue;
                        int type = MyUtils.parseInt(split[1]);
                        Integer parentId = MyUtils.parseInt(split[2]);
                        if (type == CommentType.LIKE_QUESTION) {
                            Question question = new Question();
                            question.setId(parentId);
                            question.setLikeCount(likeCount);
                            questionMapper.updateByPrimaryKeySelective(question);
                        } else if (type == CommentType.LIKE_COMMENT) {
                            Comment comment = new Comment();
                            comment.setId(parentId);
                            comment.setLikeCount(Long.valueOf(likeCount));
                            commentMapper.updateByPrimaryKeySelective(comment);
                        }
                    }
                    integerRedisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
                }
            }
        }
    }
}
