package com.liu.exceptions;

/**
 * Created by Administrator on 2017/2/19.
 * 秒杀业务所有业务异常
 */
public class SeckillException extends RuntimeException{

    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }
}
