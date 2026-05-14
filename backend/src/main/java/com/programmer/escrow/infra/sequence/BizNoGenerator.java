package com.programmer.escrow.infra.sequence;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;

@Component
public class BizNoGenerator {

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${app.sequence.demand-prefix}")
    private String demandPrefix;

    @Value("${app.sequence.quote-prefix}")
    private String quotePrefix;

    @Value("${app.sequence.order-prefix}")
    private String orderPrefix;

    @Value("${app.sequence.dispute-prefix}")
    private String disputePrefix;

    public BizNoGenerator(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public String nextDemandNo() {
        return nextNo(demandPrefix);
    }

    public String nextUserNo() {
        return nextNo("US");
    }

    public String nextQuoteNo() {
        return nextNo(quotePrefix);
    }

    public String nextOrderNo() {
        return nextNo(orderPrefix);
    }

    public String nextDisputeNo() {
        return nextNo(disputePrefix);
    }

    private String nextNo(String prefix) {
        String datePart = DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN);
        String dayPart = DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN);
        try {
            String redisKey = "seq:" + prefix + ":" + dayPart;
            Long seq = stringRedisTemplate.opsForValue().increment(redisKey);
            stringRedisTemplate.expire(redisKey, Duration.ofDays(2));
            return prefix + datePart + String.format("%04d", seq == null ? 1 : (seq % 10000));
        } catch (Exception ex) {
            return prefix + datePart + RandomUtil.randomNumbers(4);
        }
    }
}
