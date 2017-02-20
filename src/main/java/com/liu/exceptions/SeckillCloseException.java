package com.liu.exceptions;

/**
 * Created by Administrator on 2017/2/19.
 * 秒杀关闭异常
 */
public class SeckillCloseException extends SeckillException{

    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
