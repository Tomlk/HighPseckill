package org.seckill.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
        "classpath:spring/spring-dao.xml",
        "classpath:spring/spring-service.xml"
})
public class SeckillServiceTest {

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() {
        List<Seckill> list = seckillService.getSeckillList();
        logger.info("list={}",list);

    }

    @Test
    public void getById() {
        long id=1000;
        Seckill seckill = seckillService.getById(id);
        logger.info("seckill:{}",seckill);
    }


    //测试代码完整逻辑，注意可重复执行.
    @Test
    public void exportSeckillLogic() {
        long id=1002;
        Exposer exposer=seckillService.exportSeckillUrl(id);
        if(exposer.isExposed()) {
            logger.info("exposer={}", exposer);
            long phone=13502171127L;
            String md5=exposer.getMd5();
            try {
                SeckillExecution execution = seckillService.executeSeckill(id, phone, md5);
                logger.info("result={}",execution);
            } catch (RepeatKillException e) {
                logger.error(e.getMessage(),e);
            }catch (SeckillCloseException e)
            {
                logger.error(e.getMessage());
            }
        }else{
            //秒杀未开启
            logger.warn("exposer={}",exposer);
        }
        /*
        2201 [main] INFO org.seckill.service.SeckillServiceTest -
        exposer=Exposer(exposed=true, md5=95815017d422cc6be8023e16458186fd, seckillId=1001, now=0, start=0, end=0)

        2236 [main] INFO org.seckill.service.SeckillServiceTest -
        result=SeckillExecution(seckillId=1001, state=1, stateInfo=秒杀成功,
         successKilled=SuccessKilled(seckillId=1001, userPhone=13502171127, state=0, createTime=Mon May 17 10:14:53 CST 2021,
         seckill=Seckill(seckillId=1001, name=500元秒杀ipad2, number=199, startTime=Sun Nov 01 13:00:00 CST 2015, endTime=Tue Nov 02 13:00:00 CST 2021, createTime=Sun May 16 05:43:27 CST 2021)))

         */

        //exposer=
        // Exposer(exposed=true,
        // md5=039682714a004d08a41b1324d17c6260,
        // seckillId=1000, now=0, start=0, end=0)
    }

}