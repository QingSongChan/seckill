package org.seckill.service;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;

import java.util.List;

/*
 *@author:PONI_CHAN
 *@date:2018/11/12 22:27
 */
//业务接口：站在“使用者”角度设计接口
//三个方面：方法定义的粒度，参数，返回类型
public interface SeckillService {

    //查询所有秒杀记录
    List<Seckill> getSeckillList();

    //查询单个秒杀记录
    Seckill getById(long seckillId);

    //秒杀开启时输出秒杀接口地址，否则输出系统时间和秒杀倒计时时间
    Exposer exportSeckillUrl(long seckillId);

    //暴露地址时传入了md5，要用内部规则来匹配，若md5被篡改则拒绝执行
    SeckillExecution executeSeckill(long seckillId, long userphone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException;

}
