package com.liu.service;

import com.liu.dto.Exposer;
import com.liu.dto.SeckillExecution;
import com.liu.exceptions.RepeatKillException;
import com.liu.exceptions.SeckillCloseException;
import com.liu.exceptions.SeckillException;
import com.liu.po.Seckill;

import java.util.List;

/**
 * Created by Administrator on 2017/2/19.
 * 秒杀业务
 */
public interface SeckillService {

    /**
     * 返回所有秒杀商品
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 返回指定的秒杀商品
     * @param id
     * @return
     */
    Seckill getById(long id);

    /**
     * 在秒杀开启时输出秒杀接口的地址，否则输出系统时间和秒杀时间
     * @param id
     * @return
     */
    Exposer exportSeckillUrl(long id);


    SeckillExecution executeSeckill(long seckillId,long userPhone,String md5)
        throws SeckillException, SeckillCloseException, RepeatKillException;

    /**
     * 使用存储过程进行秒杀操作
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     */
    SeckillExecution executeSeckillByProducure(long seckillId,long userPhone, String md5);
}
