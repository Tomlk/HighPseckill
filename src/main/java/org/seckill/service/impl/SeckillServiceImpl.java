package org.seckill.service.impl;

import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;

//@Component 组件的实例 宽泛 @Service  @Dao  @Controller
@Service
public class SeckillServiceImpl implements SeckillService {

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    //注入Service依赖
    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;
    
    @Autowired
    private RedisDao redisDao;

    private final String slat="sadkfjalsdjfalksj23423^&*^&%&!EBJKH#e™£4"; // //md5盐值字符串,用于混淆MD5

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        // 缓存优化：通过redis缓存起来
        //1.访问redis
        Seckill seckill = redisDao.getSeckill(seckillId);
        if(Objects.isNull(seckill)){
            //2.访问数据库
            seckill=seckillDao.queryById(seckillId);
            if(Objects.isNull(seckill)){
                return new Exposer(false,seckillId);
            }else{
                //3.放入redis
                redisDao.putSeckill(seckill);
            }
        }

//        Seckill seckill=seckillDao.queryById(seckillId);
//
//        if(Objects.isNull(seckill))
//            return new Exposer(false,seckillId);
        Date startTime=seckill.getStartTime();
        Date endTime=seckill.getEndTime();
        //当前系统时间
        Date nowTime=new Date();
        if(nowTime.getTime()<startTime.getTime()
            || nowTime.getTime()>endTime.getTime())
            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());

        //转化特定字符串的过程，不可逆
        String md5=getMD5(seckillId);
        return new Exposer(true,md5,seckillId);
    }

    private String getMD5(long seckillId)
    {
        String base=seckillId+"/"+slat;
        String md5= DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }
    @Override
    @Transactional
    /**
     * 使用注解控制事务方法的优点：
     * 1:开发团队达成一致的约定，明确标注事务方法的编程风格。
     * 2:保证事务方法的执行时间尽可能短，不要穿插其他的网络操作，RPC/HTTP请求 或者剥离到事务方法外。
     * 3:不是所有的方法都需要事务，如只有一条修改操作，只读操作不需要事务控制。
     */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {

        if(Objects.isNull(md5) || !md5.equals(getMD5(seckillId))){
            throw new SeckillException("seckill data rewrite");
        }
        //执行秒杀逻辑：减库存+记录购买行为
        Date nowTime=new Date();
        int updateCount=seckillDao.reduceNumber(seckillId,nowTime);

        try {
            if(updateCount<=0)
            {
                //没有更新到记录,秒杀结束
                throw new SeckillCloseException("seckill closed");
            }
            else{
                //减库存结束,记录购买行为
                int insertCount=successKilledDao.insertSuccessKilled(seckillId,userPhone);
                //唯一：seckillId,userPhone
                if(insertCount<=0)
                {
                    //重复秒杀
                    throw new RepeatKillException("seckill repeated");
                }
                else {
                    //秒杀记录
                    SuccessKilled successKilled=successKilledDao.queryByIdWithSeckill(seckillId,userPhone);
                    return new SeckillExecution(seckillId,SeckillStatEnum.SUCCESS,successKilled);
                }
            }
        }catch (SeckillCloseException e1)
        {
            throw e1;
        }
        catch (RepeatKillException e2)
        {
            throw e2;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(),e);
            //所有编译器异常，转化为运行期异常，spring 帮我们回滚
            throw new SeckillException("seckill inner error:"+e.getMessage());
        }

    }
}
