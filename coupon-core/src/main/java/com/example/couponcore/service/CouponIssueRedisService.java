package com.example.couponcore.service;

import static com.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;

import com.example.couponcore.repository.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponIssueRedisService {
    private final RedisRepository redisRepository;


    // 수량 조회 및 발급가능 여부 검증
    public boolean availableTotalIssueQuantity(Integer totalQuantity, long couponId) {

        // 수량이 없는 쿠폰인경우 항상 true
        if (totalQuantity == null) {
            return true;
        }
        String key = getIssueRequestKey(couponId);
        return totalQuantity > redisRepository.sCard(key);
    }

    // 중복 발급 검증
    public boolean availableUserIssueQuantity(long couponId, long userId) {
        String key = getIssueRequestKey(couponId);
        return !redisRepository.sIsMember(key, String.valueOf(userId));
    }

}
