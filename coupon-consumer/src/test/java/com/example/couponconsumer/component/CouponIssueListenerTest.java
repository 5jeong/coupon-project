package com.example.couponconsumer.component;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.couponconsumer.TestConfig;
import com.example.couponcore.repository.redis.RedisRepository;
import com.example.couponcore.service.CouponIssueService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(CouponIssueListener.class)
class CouponIssueListenerTest extends TestConfig {

    @Autowired
    CouponIssueListener couponIssueListener;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    RedisRepository redisRepository;

    @MockitoBean
    CouponIssueService couponIssueService;

    @BeforeEach
    void clear() {
        Set<String> keys = redisTemplate.keys("*");
        redisTemplate.delete(keys);
    }

    @DisplayName("쿠폰 발급 큐에 처리 대상이 없으면 발급 x ")
    @Test
    void issue_1() throws JsonProcessingException {
        //when
        couponIssueListener.issue();

        //then
        verify(couponIssueService, never()).issue(anyLong(), anyLong());
    }

    @DisplayName("쿠폰 발급 큐에 처리 대상이 있으면 발급 o")
    @Test
    void issue_2() throws JsonProcessingException {
        //given
        long couponId = 1;
        long userId = 1;
        int totalQuantity = Integer.MAX_VALUE;
        redisRepository.issueRequest(couponId, userId, totalQuantity);

        //when
        couponIssueListener.issue();

        //then
        verify(couponIssueService, times(1)).issue(couponId, userId);
    }

    @DisplayName("쿠폰 발급 요청 순서에 맞게 처리된다.")
    @Test
    void issue_3() throws JsonProcessingException {
        //given
        long couponId = 1;
        int totalQuantity = Integer.MAX_VALUE;

        for (int i = 1; i <= 3; i++) {
            redisRepository.issueRequest(couponId, i, totalQuantity);
        }

        //when
        couponIssueListener.issue();

        //then
        InOrder inOrder = Mockito.inOrder(couponIssueService);
        inOrder.verify(couponIssueService, times(1)).issue(couponId,1);
        inOrder.verify(couponIssueService, times(1)).issue(couponId, 2);
        inOrder.verify(couponIssueService, times(1)).issue(couponId, 3);
    }
}