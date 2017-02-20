package com.liu.mapper;

import com.liu.po.SuccessKilled;
import org.apache.ibatis.annotations.Param;

/**
 * Created by Administrator on 2017/2/19.
 */
public interface SuccessKilledMapper {

    /**
     * 插入购买明细,可过滤重复
     * @param seckillId
     * @param userPhone
     * @return插入的行数
     */
    int insertSuccessKilled(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);


    /**
     * 根据秒杀商品的id查询明细SuccessKilled对象(该对象携带了Seckill秒杀产品对象)
     * @param seckillId
     * @return
     */
    SuccessKilled queryByIdWithSeckill(@Param("seckillId")long seckillId, @Param("userPhone") long userPhone);
}
