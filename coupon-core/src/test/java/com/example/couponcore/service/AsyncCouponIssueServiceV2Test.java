package com.example.couponcore.service;

import static com.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static com.example.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;
import static org.junit.jupiter.api.Assertions.*;

import com.example.couponcore.TestConfig;
import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import com.example.couponcore.model.Coupon;
import com.example.couponcore.model.CouponType;
import com.example.couponcore.repository.mysql.CouponRepository;
import com.example.couponcore.repository.redis.dto.CouponIssueRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

class AsyncCouponIssueServiceV2Test extends TestConfig {
    @Autowired
    AsyncCouponIssueServiceV2 asyncCouponIssueServiceV2;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    CouponRepository couponRepository;


    @BeforeEach
    void clear() {
        Collection<String> keys = redisTemplate.keys("*");
        redisTemplate.delete(keys);
    }

    @DisplayName("쿠폰 발급 : 쿠폰이 존재하지 않으면 예외 반환")
    @Test
    void issue_1() {
        //given
        long couponId = 1;
        long userId = 1;

        //when & then
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            asyncCouponIssueServiceV2.issue(couponId, userId);
        });
        assertEquals(exception.getErrorCode(), ErrorCode.COUPON_NOT_EXIST);
    }

    @DisplayName("쿠폰 발급 : 쿠폰 발급 수량이 존재하지 않으면 예외 반환")
    @Test
    void issue_2() {
        //given
        long userId = 100;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰 테스트")
                .totalQuantity(10)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();

        couponRepository.save(coupon);
        for (int i = 1; i <= 10; i++) {
            redisTemplate.opsForSet().add(getIssueRequestKey(coupon.getId()), String.valueOf(i));
        }

        //when
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            asyncCouponIssueServiceV2.issue(coupon.getId(), userId);
        });

        //then
        assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);

    }

    @DisplayName("쿠폰 발급 : 이미 쿠폰이 발급된 유저면 예외 반환")
    @Test
    void issue_3() {
        //given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰 테스트")
                .totalQuantity(10)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();

        couponRepository.save(coupon);
        redisTemplate.opsForSet().add(getIssueRequestKey(coupon.getId()), String.valueOf(userId));

        //when
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            asyncCouponIssueServiceV2.issue(coupon.getId(), userId);
        });

        //then
        assertEquals(exception.getErrorCode(), ErrorCode.DUPLICATED_COUPON_ISSUE);

    }

    @DisplayName("쿠폰 발급 : 쿠폰발급기간이 유효하지 않으면 예외 반환")
    @Test
    void issue_4() {
        //given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰 테스트")
                .totalQuantity(10)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();

        couponRepository.save(coupon);
        redisTemplate.opsForSet().add(getIssueRequestKey(coupon.getId()), String.valueOf(userId));

        //when
        CouponIssueException exception = Assertions.assertThrows(CouponIssueException.class, () -> {
            asyncCouponIssueServiceV2.issue(coupon.getId(), userId);
        });

        //then
        assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_DATE);

    }


    @DisplayName("쿠폰 발급 : 쿠폰 발급이 성공하면 유저가 redis에 저장")
    @Test
    void issue_5() {
        //given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰 테스트")
                .totalQuantity(10)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(3))
                .build();

        couponRepository.save(coupon);

        //when
        asyncCouponIssueServiceV2.issue(coupon.getId(), userId);

        //then
        Boolean isSaved = redisTemplate.opsForSet()
                .isMember(getIssueRequestKey(coupon.getId()), String.valueOf(userId));
        assertTrue(isSaved);
    }

    @DisplayName("쿠폰 발급 : 쿠폰 발급이 성공하면 쿠폰 발급 큐에 저장")
    @Test
    void issue_6() throws JsonProcessingException {
        //given
        long userId = 1;
        Coupon coupon = Coupon.builder()
                .couponType(CouponType.FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰 테스트")
                .totalQuantity(10)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(3))
                .build();

        couponRepository.save(coupon);
        CouponIssueRequest couponIssueRequest = new CouponIssueRequest(coupon.getId(), userId);
        //when
        asyncCouponIssueServiceV2.issue(coupon.getId(), userId);

        //then
        String savedIssueRequest = redisTemplate.opsForList().leftPop(getIssueRequestQueueKey());

        assertEquals(new ObjectMapper().writeValueAsString(couponIssueRequest), savedIssueRequest);

    }
}