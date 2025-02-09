package com.example.couponapi.service;

import com.example.couponapi.dto.CouponIssueRequest;
import com.example.couponcore.component.DistributeLockExecutor;
import com.example.couponcore.service.AsyncCouponIssueServiceV1;
import com.example.couponcore.service.AsyncCouponIssueServiceV2;
import com.example.couponcore.service.CouponIssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class CouponIssueRequestService {

    private final CouponIssueService couponIssueService;
    private final DistributeLockExecutor distributeLockExecutor;
    private final AsyncCouponIssueServiceV1 asyncCouponIssueServiceV1;
    private final AsyncCouponIssueServiceV2 asyncCouponIssueServiceV2;

    public void issueRequestV1(CouponIssueRequest request) {
        // Redisson  분산락 적용
//        distributeLockExecutor.execute("lock_" + request.couponId(), 10000, 10000,
//                () -> {couponIssueService.issue(request.couponId(), request.userId());
//        });
        couponIssueService.issue(request.couponId(), request.userId());

        log.info("쿠폰 발급 완료. couponId: {}, userId: {}", request.couponId(), request.userId());
    }

    public void asyncIssueRequestV1(CouponIssueRequest request) {
        asyncCouponIssueServiceV1.issue(request.couponId(), request.userId());

        log.info("쿠폰 발급 완료. couponId: {}, userId: {}", request.couponId(), request.userId());
    }

    public void asyncIssueRequestV2(CouponIssueRequest request) {
        asyncCouponIssueServiceV2.issue(request.couponId(), request.userId());

        log.info("쿠폰 발급 완료. couponId: {}, userId: {}", request.couponId(), request.userId());
    }

}
