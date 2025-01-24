package com.example.couponcore.service;

import static com.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.couponcore.TestConfig;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;


class CouponIssueRedisServiceTest extends TestConfig {

    @Autowired
    CouponIssueRedisService couponIssueRedisService;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void claer() {
        Collection<String> redisKeys = redisTemplate.keys("*");
        redisTemplate.delete(redisKeys);
    }

    @DisplayName("쿠폰 수량 검증 : 발급 가능 수량이 존재하면 true반환")
    @Test
    void availableTotalIssueQuantity_1() {
        //given
        int totalQuantity = 10;
        long couponId = 1;

        //when
        boolean result = couponIssueRedisService.availableTotalIssueQuantity(totalQuantity, couponId);

        //then
        assertTrue(result);
    }

    @DisplayName("쿠폰 수량 검증 : 발급 가능 수량이 없으면 false반환")
    @Test
    void availableTotalIssueQuantity_2() {
        //given
        int totalQuantity = 10;
        long couponId = 1;

        for (int i = 1; i <= 10; i++) {
            redisTemplate.opsForSet().add(getIssueRequestKey(couponId), String.valueOf(i));
        }

        //when
        boolean result = couponIssueRedisService.availableTotalIssueQuantity(totalQuantity, couponId);

        //then
        assertFalse(result);
    }

    @DisplayName("쿠폰 중복 발급 검증 : 발급된 내역에 유저가 존재하지 않으면 true를 반환")
    @Test
    void availableUserIssueQuantity_1() {
        //given
        long couponId = 1;
        long userId = 1;
        //when
        boolean result = couponIssueRedisService.availableUserIssueQuantity(couponId, userId);
        //then
        assertTrue(result);
    }

    @DisplayName("쿠폰 중복 발급 검증 : 발급된 내역에 유저가 존재하면 false를 반환")
    @Test
    void availableUserIssueQuantity_2() {
        //given
        long couponId = 1;
        long userId = 1;
        redisTemplate.opsForSet().add(getIssueRequestKey(couponId), String.valueOf(userId));
        //when
        boolean result = couponIssueRedisService.availableUserIssueQuantity(couponId, userId);
        //then
        assertFalse(result);
    }
}