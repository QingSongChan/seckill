package org.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/*
 *@author:PONI_CHAN
 *@date:2018/12/12 17:16
 */
public class RedisDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    //引入Jedis依赖，用来连接
    private final JedisPool jedisPool;

    public RedisDao(String ip, int port) {
        jedisPool = new JedisPool(ip, port);
    }

    //protostuff API,类的字节码对象（通过反射拿到属性、方法等）
    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    //不通过db，直接访问redis去拿到Seckill对象
    public Seckill getSeckill(long seckillId) {
        //redis操作逻辑
        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = "seckill" + seckillId;
                //并没有实现内部序列化操作
                //get->byte[]->反序列化->Object(Seckill)
                byte[] bytes = jedis.get(key.getBytes());
                if (bytes != null) {
                    //创建个空对象
                    Seckill seckill = schema.newMessage();
                    //bytes存放数据，按照schema的字节码模式反序列化传到seckill对象里面去
                    ProtobufIOUtil.mergeFrom(bytes, seckill, schema);

                    return seckill;
                }
            } finally {
                jedis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String putSeckill(Seckill seckill) {

        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = "seckill" + seckill.getSeckillId();
                //set Object(Seckill)->序列化->byte[],LinkedBuffer是缓存器
                byte[] bytes = ProtobufIOUtil.toByteArray(seckill, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));

                //超时缓存
                int timeout = 60 * 60;
                String result = jedis.setex(key.getBytes(), timeout, bytes);
                return result;
            } finally {
                jedis.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
