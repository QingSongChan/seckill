package org.seckill.exception;

/*
 *@author:PONI_CHAN
 *@date:2018/11/12 22:50
 */
//所有秒杀相关异常
public class SeckillException extends RuntimeException {

    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }
}
