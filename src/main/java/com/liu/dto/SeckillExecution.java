package com.liu.dto;

import com.liu.enums.SeckillStatEnum;
import com.liu.po.SuccessKilled;

/**
 * Created by Administrator on 2017/2/19.
 * 一个用于判断秒杀是否成功的信息，成功就返回秒杀成功的所有信息(包括秒杀的商品id、秒杀成功状态、成功信息、用户明细)
 * ，失败就抛出一个我们允许的异常(重复秒杀异常、秒杀结束异常),
 */
public class SeckillExecution extends RuntimeException{

    private long seckillId;
    private int state;
    private String info;
    //当秒杀成功时，需要传递秒杀成功的对象回去
    private SuccessKilled successKilled;

    /**
     * 成功时返回该信息
     * @param seckillId
     * @param successKilled
     */
    public SeckillExecution(long seckillId, SeckillStatEnum statEnum, SuccessKilled successKilled) {
        this.seckillId = seckillId;
        this.state = statEnum.getState();
        this.info = statEnum.getInfo();
        this.successKilled = successKilled;
    }

    /**
     * 失败时返回
     * @param seckillId
     */
    public SeckillExecution(long seckillId, SeckillStatEnum statEnum) {
        this.seckillId = seckillId;
        this.state = statEnum.getState();
        this.info = statEnum.getInfo();
    }

    public long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(long seckillId) {
        this.seckillId = seckillId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public SuccessKilled getSuccessKilled() {
        return successKilled;
    }

    public void setSuccessKilled(SuccessKilled successKilled) {
        this.successKilled = successKilled;
    }

    @Override
    public String toString() {
        return "SeckillExecution{" +
                "seckillId=" + seckillId +
                ", state=" + state +
                ", info='" + info + '\'' +
                ", successKilled=" + successKilled +
                '}';
    }
}
