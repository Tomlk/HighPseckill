package org.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisDao {

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    private final JedisPool jedisPool;

    public RedisDao(String ip,int port){
        jedisPool=new JedisPool(ip,port);
    }

    private RuntimeSchema<Seckill> schema=RuntimeSchema.createFrom(Seckill.class);

    public Seckill getSeckill(long seckillId){
        //redis操作逻辑
        try{
            Jedis jedis=jedisPool.getResource();
            try{
                String key="seckill:"+seckillId;
                //并没有实现内部序列化操作
                //get->二进制byte[]->反序列化->Object(Seckill)
                //自定义序列化 ->缓存
                //protostuff:pojo.


                byte[] bytes = jedis.get(key.getBytes());
                if(bytes!=null){
                    Seckill seckill = schema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes,seckill,schema);
                    //seckill 被反序列化
                    return seckill; //压缩到1/10的空间
                }
            }finally {
                jedis.close();
            }

        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    public String putSeckill(Seckill seckill){
        //seckill对象传递到redis中
        //Object(seckill)->序列化->bytes[]

        try{
            Jedis jedis=jedisPool.getResource();
            try{
                String key="seckill:"+seckill.getSeckillId();
                byte[] bytes=ProtostuffIOUtil.toByteArray(seckill,schema,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));

                //超时缓存
                int timeout=60*60;//一小时
                String result = jedis.setex(key.getBytes(), timeout, bytes);
                return result;

            }finally {
                jedis.close();
            }
        }catch (Exception e){

        }
        return null;
    }
}
