package com.taptap.ratelimiter;

import com.taptap.ratelimiter.annotation.RateLimit;
import com.taptap.ratelimiter.model.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockService {
    private static final Logger logger = LoggerFactory.getLogger(MockService.class);

    /**
     * 该方法允许 3 秒内调用 5 次，key = RateLimiter_ + 类名 + 方法名
     *
     * @param shouldFail
     */
    @RateLimit(mode = Mode.TIME_WINDOW, rate = 5, rateInterval = "3s")
    public void handle(boolean shouldFail) {
        if (shouldFail) {
            throw new RevocableException();
        } else {
            logger.info("it just works");
        }
    }
}