package com.taptap.ratelimiter.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/3/18
 */
public final class LuaScript {

    private static final Logger log = LoggerFactory.getLogger(LuaScript.class);
    private static final String timeWindowRateLimiterScript;
    private static final String timeWindowRateLimiterCancelScript;
    private static final String tokenBucketRateLimiterScript;
    private static final String tokenBucketRateLimiterCancelScript;

    static {
        timeWindowRateLimiterScript = getRateLimiterScript("META-INF/timeWindow-rateLimit.lua");
        timeWindowRateLimiterCancelScript = getRateLimiterScript(
            "META-INF/timeWindow-rateLimit-cancel.lua");
        tokenBucketRateLimiterScript = getRateLimiterScript("META-INF/tokenBucket-rateLimit.lua");
        tokenBucketRateLimiterCancelScript = getRateLimiterScript(
            "META-INF/tokenBucket-rateLimit-cancel.lua");
    }

    private LuaScript() {
    }

    private static String getRateLimiterScript(String scriptFileName) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream(scriptFileName);
        try {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("tokenBucket-rateLimit.lua Initialization failure", e);
            throw new RuntimeException(e);
        }
    }

    public static String getTimeWindowRateLimiterScript() {
        return timeWindowRateLimiterScript;
    }

    public static String getTimeWindowRateLimiterCancelScript() {
        return timeWindowRateLimiterCancelScript;
    }

    public static String getTokenBucketRateLimiterScript() {
        return tokenBucketRateLimiterScript;
    }

    public static String getTokenBucketRateLimiterCancelScript() {
        return tokenBucketRateLimiterCancelScript;
    }
}
