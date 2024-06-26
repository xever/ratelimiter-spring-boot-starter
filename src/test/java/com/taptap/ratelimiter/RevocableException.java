package com.taptap.ratelimiter;

import com.taptap.ratelimiter.exception.RevocableExceptionInterface;

public class RevocableException extends RuntimeException implements
    RevocableExceptionInterface {
}