package org.seckill.dao;

import org.apache.ibatis.annotations.Param;
import org.seckill.entity.Seckill;

import java.util.Date;
import java.util.List;

/*
 *@author:PONI_CHAN
 *@date:2018/11/12 21:07
 */
public interface SeckillDao {

    /*
    减库存
    @return 如果影响行数>1,表示更新的记录行数
     */
    int reduceNumber(@Param("seckillId") long seckillId, @Param("killtime") Date killtime);

    /*
    根据ID查询秒杀对象
     */
    Seckill queryById(long seckillId);

    /*
    根据偏移量查询秒杀商品列表
     */
    List<Seckill> queryAll(@Param("offset") int offset, @Param("limit") int limit);

}
