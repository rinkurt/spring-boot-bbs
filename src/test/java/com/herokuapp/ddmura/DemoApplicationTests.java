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
        Boolean aaa1 = integerRedisTemplate.hasKey("aaa");
        Long aaa = integerRedisTemplate.getExpire("aaa");
        Boolean aaa2 = integerRedisTemplate.persist("aaa");
        System.out.println(aaa1);
        System.out.println(aaa);
        System.out.println(aaa2);

        System.out.println(integerRedisTemplate.hasKey("bbb"));
        System.out.println(integerRedisTemplate.getExpire("bbb"));
        System.out.println(integerRedisTemplate.persist("bbb"));

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
