package com.herokuapp.ddmura;

import com.herokuapp.ddmura.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.util.CastUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SpringBootTest
class DemoApplicationTests {


    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedisTemplate<String, Integer> integerRedisTemplate;


    @Test
    void testRedis() {

    }


    @Test
    void contextLoads() {

    }

}
