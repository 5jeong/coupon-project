package com.example.couponcore.component;

import com.example.couponcore.model.event.CouponIssueCompleteEvent;
import com.example.couponcore.service.CouponCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponEventListener {

    private final CouponCacheService couponCacheService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void issueComplete(CouponIssueCompleteEvent event) {
        log.info("issue complete. cache refresh start couponId: {}", event.couponId());
        couponCacheService.putCouponCache(event.couponId()); // redis 캐시 업데이트
        couponCacheService.putCouponLocalCache(event.couponId()); // 로컬 캐시 업데이트
        log.info("issue complete. cache refresh end couponId: {}", event.couponId());
    }
}
