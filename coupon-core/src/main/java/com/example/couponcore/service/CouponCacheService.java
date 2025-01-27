package com.example.couponcore.service;

import com.example.couponcore.model.Coupon;
import com.example.couponcore.repository.redis.dto.CouponRedisEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponCacheService {

    private final CouponIssueService couponIssueService;

    /***
     * Redis 캐시를 사용하여 쿠폰 데이터를 조회
     *
     * @param couponId 조회할 쿠폰의 ID
     * @return {@link CouponRedisEntity} 객체로 변환된 쿠폰 데이터
     * @Cacheable(cacheNames = "coupon") 어노테이션을 사용하여 Redis 캐시에 저장 및 조회
     */
    @Cacheable(cacheNames = "coupon")
    public CouponRedisEntity getCouponCache(long couponId) {
        Coupon coupon = couponIssueService.findCoupon(couponId);
        return new CouponRedisEntity(coupon);
    }

    /***
     * Caffeine 로컬 캐시를 사용하여 쿠폰 데이터를 조회
     * 만약 캐시에 데이터가 없으면, Redis 캐시를 확인
     *
     * @param couponId 조회할 쿠폰의 ID
     * @return {@link CouponRedisEntity} 객체로 변환된 쿠폰 데이터
     * @Cacheable(cacheNames = "coupon", cacheManager = "localCacheManager") 어노테이션을 사용하여 로컬 캐시에 저장 및 조회
     */
    @Cacheable(cacheNames = "coupon", cacheManager = "localCacheManager")
    public CouponRedisEntity getCouponLocalCache(long couponId) {
        return proxy().getCouponCache(couponId);
    }

    @CachePut(cacheNames = "coupon")
    public CouponRedisEntity putCouponCache(long couponId){
        return getCouponCache(couponId);
    }

    @CachePut(cacheNames = "coupon",cacheManager = "localCacheManager")
    public CouponRedisEntity putCouponLocalCache(long couponId){
        return getCouponLocalCache(couponId);
    }

    /***
     * 현재 프록시 객체를 반환
     * 내부 메서드 호출 시 AOP가 적용되지 않으므로, 이 메서드를 사용해 프록시 객체를 호출
     *
     * @return 현재 클래스의 프록시 객체
     */
    private CouponCacheService proxy() {
        return ((CouponCacheService) AopContext.currentProxy());
    }
}
