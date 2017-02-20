package com.liu.service.impl;

import com.liu.dto.Exposer;
import com.liu.dto.SeckillExecution;
import com.liu.enums.SeckillStatEnum;
import com.liu.exceptions.RepeatKillException;
import com.liu.exceptions.SeckillCloseException;
import com.liu.exceptions.SeckillException;
import com.liu.mapper.SeckillMapper;
import com.liu.mapper.SuccessKilledMapper;
import com.liu.po.Seckill;
import com.liu.po.SuccessKilled;
import com.liu.service.SeckillService;
import org.apache.commons.collections.MapUtils;
import org.omg.CORBA.OBJ_ADAPTER;
import org.omg.CORBA.Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/19.
 */
@Service
public class SeckillServiceImpl implements SeckillService{

    /**
     * 假如日志类
     */
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //MD5的盐值，用于md5的混淆处理
    private final String salt = "sdhkahd;'l';;sdlajsljdlaj";

    @Autowired
    private SeckillMapper seckillMapper;

    @Autowired
    private SuccessKilledMapper successKilledMapper;

    public List<Seckill> getSeckillList() {
        return seckillMapper.queryAll(0,4);
    }

    public Seckill getById(long id) {
        return seckillMapper.queryById(id);
    }

    /**
     * 暴露接口
     * 分为三种情况：
     * 1. 秒杀商品不存在了
     * 2. 秒杀未开启，时间未到
     * 3. 秒杀成功
     * 只读操作不开启事务
     * @param id
     * @return
     */
    public Exposer exportSeckillUrl(long id) {
        Seckill seckill = seckillMapper.queryById(id);
        //秒杀商品不存在
        if(seckill == null){
            System.out.println("1");
            return new Exposer(false, id);
        }

        //如果是秒杀未开启
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();
        if(nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()){
            System.out.println("2");
            return new Exposer(false, id, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }
        String md5 = getMd5(id);
        return new Exposer(true, md5, id);
    }

    /**
     * 只有上面方法秒杀成功时，即秒杀开关打开时，才执行秒杀,,秒杀操作
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     * @throws SeckillException
     * @throws SeckillCloseException
     * @throws RepeatKillException
     *
     * 开启事务
     * 使用注解控制事务方法的优点:
     * 1.开发团队达成一致约定，明确标注事务方法的编程风格
     * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/HTTP请求或者剥离到事务方法外部
     * 3.不是所有的方法都需要事务，如只有一条修改操作、只读操作不要事务控
     */
    @Transactional
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, SeckillCloseException, RepeatKillException {

        //有可能当用户拿到我们的url后，破解我们的md5来执行秒杀
        if(md5 == null || !md5.equals(getMd5(seckillId))){
            throw new SeckillException("秒杀数据被重写");//当秒杀数据被重写后，抛出异常
        }

        Date nowTime = new Date();
        //首先要减库存
        try {
            int i = seckillMapper.reduceNumber(seckillId, nowTime);
            //如果减库存失败，说明秒杀已经结束
            if(i <= 0){
                throw new SeckillCloseException("秒杀已经结束");
            }else{
                int insertCount = successKilledMapper.insertSuccessKilled(seckillId, userPhone);
                //如果插入秒杀记录失败,看是否该明细被重复插入，即用户是否重复秒杀
                if(insertCount <= 0){
                    throw new RepeatKillException("重复秒杀");
                }else{
                    //秒杀成功,得到成功插入的明细记录,并返回成功秒杀的信息
                    SuccessKilled successKilled = successKilledMapper.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
                }
            }
        } catch (SeckillCloseException e1){
            throw e1;
        } catch (RepeatKillException e2){
            throw e2;
        } catch (Exception e){
            logger.error(e.getMessage());
            throw new SeckillException("秒杀系统异常" + e.getMessage());
        }

    }

    public SeckillExecution executeSeckillByProducure(long seckillId, long userPhone, String md5) {
        if(md5 == null || !md5.equals(getMd5(seckillId))){//数据篡改
            return new SeckillExecution(seckillId, SeckillStatEnum.DATE_REWRITE);
        }
        Date now = new Date();
        Map params = new HashMap();
        params.put("seckillId", seckillId);
        params.put("phone", userPhone);
        params.put("killTime", now);
        params.put("result", null);
        try {
            seckillMapper.seckillByProduce(params);
            Integer result = MapUtils.getInteger(params, "result", -3);
            if(result == 1){//成功
                SuccessKilled successKilled = successKilledMapper.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
            }else{//如果失败，把错误状态-1或者-2等返回
                return new SeckillExecution(seckillId, SeckillStatEnum.stateOf(result));
            }
        } catch (Exception e){//出现异常也要返回
            logger.error(e.getMessage(), e);
            return new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
        }
    }

    /**
     * 使用spring的md5工具加密
     * @param seckillId
     * @return
     */
    private String getMd5(long seckillId){
        String md5 = seckillId + "/" + salt;
        md5 = DigestUtils.md5DigestAsHex(md5.getBytes());
        return md5;
    }
}
