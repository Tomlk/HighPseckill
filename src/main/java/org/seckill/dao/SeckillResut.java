package org.seckill.dao;

import lombok.Getter;
import lombok.Setter;

//所有ajax请求返回类型，封装json结果
@Getter
@Setter
public class SeckillResut<T> {

    private boolean success;

    private T data;

    private String error;

    public SeckillResut(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public SeckillResut(boolean success, String error) {
        this.success = success;
        this.error = error;
    }
}
