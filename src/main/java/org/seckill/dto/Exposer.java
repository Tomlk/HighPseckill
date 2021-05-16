package org.seckill.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 暴露秒杀地址
 *
 */
@Getter
@Setter
@ToString
public class Exposer {

    //是否开启秒杀
    private boolean exposed;

    private String md5; //一种加密措施

    private long seckillId;

    private long now; //系统当前时间(ms)

    private long start; //开始时间

    private long end;//结束时间

    public Exposer(boolean exposed, String md5, long seckillId) {
        this.exposed = exposed;
        this.md5 = md5;
        this.seckillId = seckillId;
    }

    public Exposer(boolean exposed, long seckillId, long now, long start, long end) {
        this.exposed = exposed;
        this.seckillId=seckillId;
        this.now = now;
        this.start = start;
        this.end = end;
    }

    public Exposer(boolean exposed, long seckillId) {
        this.exposed = exposed;
        this.seckillId = seckillId;
    }
}
