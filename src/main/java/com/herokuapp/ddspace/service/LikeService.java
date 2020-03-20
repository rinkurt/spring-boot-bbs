package com.herokuapp.ddspace.service;

import com.herokuapp.ddspace.dto.CommentType;
import com.herokuapp.ddspace.dto.LikeNotifyDTO;
import com.herokuapp.ddspace.enums.ResultEnum;
import com.herokuapp.ddspace.mapper.CommentMapper;
import com.herokuapp.ddspace.mapper.LikesMapper;
import com.herokuapp.ddspace.mapper.QuestionMapper;
import com.herokuapp.ddspace.model.*;
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
        // 对于即将写入的key，取消过期时间，表示“脏位”
        Boolean persistSuccess = integerRedisTemplate.persist("like-count:" + type + ":" + parentId);
        if (persistSuccess == null || !persistSuccess) {
            Integer result = integerRedisTemplate.opsForValue().get("like-count:" + type + ":" + parentId);
            if (result == null) {
                if (type == CommentType.LIKE_QUESTION) {
                    Question question = questionMapper.selectByPrimaryKey(parentId);
                    result = question.getLikeCount();
                } else if (type == CommentType.LIKE_COMMENT) {
                    Comment comment = commentMapper.selectByPrimaryKey(parentId);
                    result = Math.toIntExact(comment.getLikeCount());
                } else {
                    result = 0;
                }
                integerRedisTemplate.opsForValue().set("like-count:" + type + ":" + parentId, result);
            }
        }
        integerRedisTemplate.expire("like-count:" + type + ":" + parentId, expireTime, TimeUnit.SECONDS);
        integerRedisTemplate.multi();
        // 移除相反id，添加当前id（可正可负）
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


    public Set<User> getLikeUserSet(Integer parentId, Integer type) {
        Set<Integer> likeIdSet = getLikeIdSet(parentId, type);
        Set<User> result = new HashSet<>();
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
