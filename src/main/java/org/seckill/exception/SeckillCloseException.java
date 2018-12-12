package org.seckill.exception;

/*
 *@author:PONI_CHAN
 *@date:2018/11/12 22:49
 */
//秒杀关闭异常
public class SeckillCloseException extends SeckillException {

    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
