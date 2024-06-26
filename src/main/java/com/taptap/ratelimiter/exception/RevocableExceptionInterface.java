package com.taptap.ratelimiter.exception;

/**
 * 如果业务代码抛出实现了该接口的异常，限流计数器应当不计入这次请求
 */
public interface RevocableExceptionInterface {

}