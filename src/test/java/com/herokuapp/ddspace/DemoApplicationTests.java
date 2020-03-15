package com.herokuapp.ddspace;

import com.alibaba.fastjson.JSON;
import com.herokuapp.ddspace.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

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
        integerRedisTemplate.opsForSet().add("aa", 1);
        integerRedisTemplate.opsForSet().add("aa", 2);
        integerRedisTemplate.opsForSet().add("aa", 3);
    }

    @Test
    void testRedis2() {
        stringRedisTemplate.setEnableTransactionSupport(true);
        stringRedisTemplate.watch("a");
        stringRedisTemplate.multi();
        stringRedisTemplate.rename("a", "b");
		stringRedisTemplate.opsForSet().members("b");
		List<Object> exec = stringRedisTemplate.exec();
		if (exec.size() > 0) {
			Set<Object> set = (Set<Object>) exec.get(0);
			for (Object o : set) {
				System.out.println(o);
			}
		}

	}

    @Test
    void contextLoads() {
    }

}
