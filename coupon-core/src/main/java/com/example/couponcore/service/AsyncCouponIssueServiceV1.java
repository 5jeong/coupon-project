package com.example.couponcore.service;

import static com.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static com.example.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;
import static com.example.couponcore.util.CouponRedisUtils.getRedisLockName;

import com.example.couponcore.component.DistributeLockExecutor;
import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import com.example.couponcore.model.Coupon;
import com.example.couponcore.repository.redis.RedisRepository;
import com.example.couponcore.repository.redis.dto.CouponIssueRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AsyncCouponIssueServiceV1 {

    private final RedisRepository redisRepository;
    private final CouponIssueRedisService couponIssueRedisService;
    private final CouponIssueService couponIssueService;
    private final DistributeLockExecutor distributeLockExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void issue(long couponId, long userId) {

        // 쿠폰 존재 검증
        Coupon coupon = couponIssueService.findCoupon(couponId);

        // 발급 가능한 날짜 검증
        if (!coupon.validateIssueDate()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_DATE);
        }

        // Redis Lock 적용
        distributeLockExecutor.execute(getRedisLockName(couponId), 3000, 3000, () -> {
            // 수량 조회 및 발급 가능 여부 검증
            if (!couponIssueRedisService.availableTotalIssueQuantity(coupon.getTotalQuantity(), couponId)) {
                throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
            }
            // 중복 발급 요청 검증
            if (!couponIssueRedisService.availableUserIssueQuantity(couponId, userId)) {
                throw new CouponIssueException(ErrorCode.DUPLICATED_COUPON_ISSUE);
            }
            issueRequest(couponId, userId);
        });

    }

    private void issueRequest(long couponId, long userId) {
        CouponIssueRequest couponIssueRequest = new CouponIssueRequest(couponId, userId);
        try {
            String value = objectMapper.writeValueAsString(couponIssueRequest);
            redisRepository.sAdd(getIssueRequestKey(couponId), String.valueOf(userId));
            // 발급에 성공했다면 쿠폰을 발급하고 쿠폰 발급 Queue에 적재
            redisRepository.rPush(getIssueRequestQueueKey(), value);
        } catch (JsonProcessingException e) {
            throw new CouponIssueException(ErrorCode.FAIL_COUPON_ISSUE_REQUES);
        }
    }
}