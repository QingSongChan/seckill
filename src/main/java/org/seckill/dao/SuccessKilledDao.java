package org.seckill.dao;

import org.apache.ibatis.annotations.Param;
import org.seckill.entity.SuccessKilled;

/*
 *@author:PONI_CHAN
 *@date:2018/11/12 21:07
 */
public interface SuccessKilledDao {

    /*
    插入购买明细，可过滤重复
     */
    int insertSuccessKilled(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

    /*
    根据id查询Successkilled并携带秒杀产品对象实体
     */
    SuccessKilled queryByIdWithSeckill(@Param("seckillId") long seckillId, @Param("userphone") long userphone);

}
