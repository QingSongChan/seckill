package org.seckill.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/*
 *@author:PONI_CHAN
 *@date:2018/11/12 22:56
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //注入Service依赖
    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private RedisDao redisDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    //md5盐值字符串，用于混淆Md5,不被用户猜到结果
    private final String slat = "jkdfghjksafgd4546*-/asdas@#$%^";

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {

        //优化点：缓存优化，超时的基础上维护一致性
        //1.访问redis，
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null) {
            //2.访问数据库
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null) {
                return new Exposer(false, seckillId);
            } else {
                //3.防入redis
                redisDao.putSeckill(seckill);
            }
        }
//        //所有秒杀单都要暴露秒杀接口，所以需要放入redis来缓存优化
//        Seckill seckill = seckillDao.queryById(seckillId);
//        if (seckill == null) {
//            return new Exposer(false, seckillId);
//        }
        Date starttime = seckill.getStartTime();
        Date endtime = seckill.getEndTime();
        Date nowtime = new Date();
        if (nowtime.getTime() < starttime.getTime() || nowtime.getTime() > endtime.getTime()) {
            return new Exposer(false, seckillId, nowtime.getTime(), starttime.getTime(), endtime.getTime());
        }
        //转化特定字符串的过程，不可逆
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);

    }

    private String getMD5(long seckillId) {
        String base = seckillId + "/" + slat;
        // String md5 = DigestUtils.md5Hex(base);
        String md5 = DigestUtils.md5Hex(base.getBytes());
        return md5;
    }

    @Override
    @Transactional
    /**
     * 使用注解控制事务方法的优点：
     * 1.开发团队达成一致约定，明确标注事务方法的编程风格
     * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/HTTP请求或者剥离到事务方法外部
     * 3.不是所有的方法都需要事务，如只有一条修改操作，只读操作不需要事务控制，
     */
    public SeckillExecution executeSeckill(long seckillId, long userphone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        //执行秒杀操作，首先判断MD5的值是否匹配
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            throw new SeckillException("seckill data and md5 rewrite");
        }

        //执行秒杀逻辑，减库存 + 记录购买行为
        Date nowTime = new Date();
        try {
            //减库存
            int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
            if (updateCount <= 0) {
                //没有更新到记录，秒杀结束
                throw new SeckillCloseException("seckill is closed");
            } else {
                //记录购买行为(唯一的ID和phone)
                int insertCount = successKilledDao.insertSuccessKilled(seckillId, userphone);
                if (insertCount <= 0) {
                    //重复秒杀
                    throw new RepeatKillException("seckill repeated");
                } else {
                    //秒杀成功
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userphone);
                    return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
                }
            }
        } catch (SeckillCloseException e1) {
            throw e1;
        } catch (RepeatKillException e2) {
            throw e2;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SeckillException("seckill inner errot:" + e.getMessage());
        }

    }
}
