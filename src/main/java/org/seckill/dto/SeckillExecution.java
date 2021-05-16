package org.seckill.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;

/**
 * 封装秒杀执行后的结果
 */
@Getter
@Setter
public class SeckillExecution {

    private long seckillId;

    private int state; //秒杀执行结果状态

    private String stateInfo;//状态标示

    private SuccessKilled successKilled;//秒杀成功对象

    public SeckillExecution(long seckillId, SeckillStatEnum statEnum, SuccessKilled successKilled) {
        this.seckillId = seckillId;
        this.state = statEnum.getState();
        this.stateInfo = statEnum.getStateInfo();
        this.successKilled = successKilled;
    }

    public SeckillExecution(long seckillId,SeckillStatEnum statEnum ) {
        this.seckillId = seckillId;
        this.state = statEnum.getState();
        this.stateInfo = statEnum.getStateInfo();
    }
}
