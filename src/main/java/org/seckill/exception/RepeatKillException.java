package org.seckill.exception;

import org.seckill.dto.SeckillExecution;

/*
 *@author:PONI_CHAN
 *@date:2018/11/12 22:45
 */
public class RepeatKillException extends SeckillException {

    public RepeatKillException(String message) {
        super(message);
    }

    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }
}
