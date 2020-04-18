package com.herokuapp.ddmura.service;

import com.herokuapp.ddmura.enums.CommentType;
import com.herokuapp.ddmura.dto.LikeNotifyDTO;
import com.herokuapp.ddmura.enums.ResultEnum;
import com.herokuapp.ddmura.mapper.CommentMapper;
import com.herokuapp.ddmura.mapper.LikesMapper;
import com.herokuapp.ddmura.mapper.QuestionMapper;
import com.herokuapp.ddmura.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class LikeService {

    @Value("${cache.read-expire-time}")
    private Integer expireTime;

    @Value("${cache.empty-expire-time}")
    private Integer emptyExpireTime;

    @Value("${cache.db-cache-expire-time}")
    Integer dbCacheExpireTime;

    @Autowired
    private RedisTemplate<String, Integer> integerRedisTemplate;

    @Autowired
    private LikesMapper likesMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserService userService;


    /**
     * 点赞，传入负的 userId 表示取消
     */
    public ResultEnum like(Integer parentId, Integer type, Integer userId) {
        String countKey = "like-count:" + type + ":" + parentId;
        String writeKey = "like:write:" + type + ":" + parentId;
        // 对于即将写入的key，取消过期时间，表示“脏位”
        integerRedisTemplate.persist(countKey);
        Boolean hasKey = integerRedisTemplate.hasKey(countKey);
        if (hasKey != null && !hasKey) {
            int result = 0;
            if (type == CommentType.LIKE_QUESTION) {
                Question question = questionMapper.selectByPrimaryKey(parentId);
                result = question.getLikeCount();
            } else if (type == CommentType.LIKE_COMMENT) {
                Comment comment = commentMapper.selectByPrimaryKey(parentId);
                result = Math.toIntExact(comment.getLikeCount());
            }
            integerRedisTemplate.opsForValue().set(countKey, result);
        }
        long num = userId > 0 ? 1 : -1;
        integerRedisTemplate.multi();
        // 移除相反id，添加当前id（可正可负）
        integerRedisTemplate.opsForSet().remove(writeKey, -userId);
        integerRedisTemplate.opsForSet().add(writeKey, userId);
        integerRedisTemplate.opsForValue().increment(countKey, num);
        integerRedisTemplate.exec();
        return ResultEnum.SUCCESS;
    }


    public Set<Integer> syncReadSetFromDatabase(Integer parentId, Integer type) {
        LikesExample likesExample = new LikesExample();
        likesExample.createCriteria()
                .andParentIdEqualTo(parentId)
                .andTypeEqualTo(type);
        List<Likes> likesList = likesMapper.selectByExample(likesExample);
        if (likesList.size() > 0) {
            // write redis
            Integer[] values = new Integer[likesList.size()];
            int i = 0;
            for (Likes likes : likesList) {
                values[i] = likes.getUserId();
                i++;
            }
            integerRedisTemplate.opsForSet().add("like:read:" + type + ":" + parentId, values);
            integerRedisTemplate.expire("like:read:" + type + ":" + parentId, expireTime, TimeUnit.SECONDS);
            return new HashSet<>(Arrays.asList(values));
        } else {
            // 向缓存插入 0 用于占位，防止重复读库
            integerRedisTemplate.opsForSet().add("like:read:" + type + ":" + parentId, 0);
            integerRedisTemplate.expire("like:read:" + type + ":" + parentId, emptyExpireTime, TimeUnit.SECONDS);
            return new HashSet<>();
        }
    }

    public void syncWriteSetToDatabase() {
        Set<String> writeKeys = integerRedisTemplate.keys("like:write*");
        if (writeKeys != null && writeKeys.size() > 0) {
            for (String key : writeKeys) {
                String[] split = key.split(":");
                if (split.length < 4) continue;
                int type, parentId;
                try {
                    type = Integer.parseInt(split[2]);
                    parentId = Integer.parseInt(split[3]);
                } catch (NumberFormatException e) {
                    continue;
                }
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

    }



    public void syncCountSetToDatabase() {
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
                        int parentId, type;
                        try {
                            type = Integer.parseInt(split[1]);
                            parentId = Integer.parseInt(split[2]);
                        } catch (NumberFormatException e) {
                            continue;
                        }
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


    public Set<Integer> getLikeIdSet(Integer parentId, Integer type) {
        Set<Integer> writeSet = integerRedisTemplate.opsForSet().members("like:write:" + type + ":" + parentId);
        Set<Integer> readSet = integerRedisTemplate.opsForSet().members("like:read:" + type + ":" + parentId);
        if (readSet == null || readSet.size() == 0) {
            readSet = syncReadSetFromDatabase(parentId, type);
        } else {
            // 延续过期时间
            if (readSet.size() == 1 && readSet.contains(0)) {
                integerRedisTemplate.expire("like:read:" + type + ":" + parentId, emptyExpireTime, TimeUnit.SECONDS);
            } else {
                integerRedisTemplate.expire("like:read:" + type + ":" + parentId, expireTime, TimeUnit.SECONDS);
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
        // 移除占位元素 0
        readSet.remove(0);
        return readSet;
    }


    public List<User> getLikeUserList(Integer parentId, Integer type) {
        Set<Integer> likeIdSet = getLikeIdSet(parentId, type);
        List<User> result = new ArrayList<>();
        for (Integer userId : likeIdSet) {
            result.add(userService.getById(userId));
        }
        return result;
    }


    public Set<Integer> randomIdFromLikeSet(Integer parentId, Integer type, int count) {
        Set<Integer> result = new HashSet<>();
        int curCount = 0;
        Set<Integer> writeSet = integerRedisTemplate.opsForSet().members("like:write:" + type + ":" + parentId);
        if (writeSet != null) {
            for (Integer id : writeSet) {
                if (id > 0) {
                    result.add(id);
                    curCount++;
                }
                if (curCount == count) {
                    return result;
                }
            }
        }
        Set<Integer> randomMembers = integerRedisTemplate.opsForSet().distinctRandomMembers(
                "like:read:" + type + ":" + parentId, count - curCount + 1); // 多拿一个，防止0元素
        if (randomMembers == null || randomMembers.size() == 0) {
            Set<Integer> readSetFromDatabase = syncReadSetFromDatabase(parentId, type);
            for (Integer id : readSetFromDatabase) {
                if (id > 0) {
                    result.add(id);
                    curCount++;
                }
                if (curCount == count) {
                    return result;
                }
            }
        } else {
            result.addAll(randomMembers);
        }
        result.remove(0);
        return result;
    }


    public boolean isContained(Integer parentId, Integer type, Integer userId) {
        Set<Integer> likeSet = getLikeIdSet(parentId, type);
        return likeSet.contains(userId);
    }


    // TODO: 分布式锁
    public Integer getLikeCount(Integer parentId, Integer type) {
        Integer result = integerRedisTemplate.opsForValue().get("like-count:" + type + ":" + parentId);
        if (result == null) {
            if (type == CommentType.LIKE_QUESTION) {
                Question question = questionMapper.selectByPrimaryKey(parentId);
                result = question.getLikeCount();
            } else if (type == CommentType.LIKE_COMMENT) {
                Comment comment = commentMapper.selectByPrimaryKey(parentId);
                result = Math.toIntExact(comment.getLikeCount());
            } else {
                return null;
            }
            integerRedisTemplate.opsForValue().set("like-count:" + type + ":" + parentId, result);
        }
        if (result == 0) {
            integerRedisTemplate.expire("like-count:" + type + ":" + parentId, emptyExpireTime, TimeUnit.SECONDS);
        } else {
            integerRedisTemplate.expire("like-count:" + type + ":" + parentId, expireTime, TimeUnit.SECONDS);
        }
        return result;
    }


    public List<User> getRandomLikeUser(Integer parentId, Integer type) {
        List<User> senders = new ArrayList<>();
        int curCount = 0, count = 2;
        Set<Integer> randomMembers = randomIdFromLikeSet(parentId, type, count);
        for (Integer userId : randomMembers) {
            senders.add(userService.getById(userId));
            curCount++;
            if (curCount >= count) break;
        }
        return senders;
    }


    public List<LikeNotifyDTO> listLikeByReceiver(Integer receiver) {
        List<LikeNotifyDTO> result = new ArrayList<>();
        QuestionExample questionExample = new QuestionExample();
        questionExample.createCriteria().andCreatorEqualTo(receiver);
        List<Question> questions = questionMapper.selectByExample(questionExample);
        for (Question question : questions) {
            int likeCount = getLikeCount(question.getId(), CommentType.LIKE_QUESTION);
            if (likeCount > 0) {
                LikeNotifyDTO likeNotifyDTO = new LikeNotifyDTO();
                likeNotifyDTO.setParentId(question.getId());
                likeNotifyDTO.setContent(question.getTitle());
                likeNotifyDTO.setType(CommentType.LIKE_QUESTION);
                likeNotifyDTO.setSenders(getRandomLikeUser(likeNotifyDTO.getParentId(), likeNotifyDTO.getType()));
                likeNotifyDTO.setCount(likeCount);
                result.add(likeNotifyDTO);
            }
        }
        CommentExample commentExample = new CommentExample();
        commentExample.createCriteria().andUserIdEqualTo(receiver);
        List<Comment> comments = commentMapper.selectByExample(commentExample);
        for (Comment comment : comments) {
            int likeCount = getLikeCount(comment.getId(), CommentType.LIKE_COMMENT);
            if (likeCount > 0) {
                LikeNotifyDTO likeNotifyDTO = new LikeNotifyDTO();
                likeNotifyDTO.setParentId(comment.getId());
                likeNotifyDTO.setContent(comment.getContent());
                likeNotifyDTO.setType(CommentType.LIKE_COMMENT);
                likeNotifyDTO.setSenders(getRandomLikeUser(likeNotifyDTO.getParentId(), likeNotifyDTO.getType()));
                likeNotifyDTO.setCount(likeCount);
                result.add(likeNotifyDTO);
            }
        }
        return result;

    }


}
