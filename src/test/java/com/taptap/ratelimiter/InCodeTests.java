package com.taptap.ratelimiter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.taptap.ratelimiter.core.RateLimiterService;
import com.taptap.ratelimiter.exception.RateLimitException;
import com.taptap.ratelimiter.model.Mode;
import com.taptap.ratelimiter.model.Result;
import com.taptap.ratelimiter.model.Rule;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class InCodeTests {

    private static final Logger logger = LoggerFactory.getLogger(InCodeTests.class);

    @Autowired
    private RateLimiterService rateLimiterService;
    @Autowired
    private MockService mockService;

    /**
     * 使用时间窗时撤销一些失败的请求的计数
     */
    @Test
    void howToRevokeWithRateLimiterServiceAndTimeWindowTest() {
        String key = UUID.randomUUID().toString();
        logger.info("key:{}", key);
        Rule rule = new Rule(Mode.TIME_WINDOW);
        rule.setKey(key);
        rule.setRate(5);
        rule.setRateInterval(10 * 60);
        while (true) {
            Result result = rateLimiterService.isAllowed(rule);
            if (!result.isAllow()) {
                rateLimiterService.revoke(rule);
                break;
            }
        }
        rateLimiterService.revoke(rule);
        Result result = rateLimiterService.isAllowed(rule);
        assertTrue(result.isAllow());
    }

    /**
     * 令牌桶撤销失败情况的请求计数
     * <p>
     * 令牌桶容量为 10，每秒产生 1 个令牌
     */
    @Test
    void howToRevokeWithRateLimiterServiceAndTokenBucketTest() {
        String key = UUID.randomUUID().toString();
        logger.info("key:{}", key);
        Rule rule = new Rule(Mode.TOKEN_BUCKET);
        rule.setKey(key);
        rule.setBucketCapacity(10);
        rule.setRate(1);
        rule.setRequestedTokens(6);
        // 第一次成功获得 6 个令牌
        Result result = rateLimiterService.isAllowed(rule);
        assertTrue(result.isAllow());
        // 第二次成功获得 4 个令牌
        rule.setRequestedTokens(4);
        result = rateLimiterService.isAllowed(rule);
        assertTrue(result.isAllow());
        // 此后不能再获取新的令牌
        result = rateLimiterService.isAllowed(rule);
        assertFalse(result.isAllow());
        // 第一次获取令牌的服务执行失败，返还这 6 个令牌
        rule.setRequestedTokens(6);
        rateLimiterService.revoke(rule);
        // 可以再次获取 6 个令牌
        result = rateLimiterService.isAllowed(rule);
        assertTrue(result.isAllow());
    }

    /**
     * 使用 @RateLimit 注解的情况下抛出特定异常可以撤销计数
     */
    @Test
    void howToRevokeWithAnnotationTest() {
        for (int i = 0; i < 10; i++) {
            try {
                mockService.handle(i % 2 == 0);
            } catch (Exception ignored) {
            }
        }
        assertThrows(RateLimitException.class, () -> mockService.handle(false));
    }


}
