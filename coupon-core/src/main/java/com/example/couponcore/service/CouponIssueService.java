package com.example.couponcore.service;

import static com.example.couponcore.exception.ErrorCode.COUPON_NOT_EXIST;
import static com.example.couponcore.exception.ErrorCode.DUPLICATED_COUPON_ISSUE;

import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.model.Coupon;
import com.example.couponcore.model.CouponIssue;
import com.example.couponcore.model.event.CouponIssueCompleteEvent;
import com.example.couponcore.repository.mysql.CouponIssueRepository;
import com.example.couponcore.repository.mysql.CouponRepository;
import com.example.couponcore.repository.mysql.CustomCouponIssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CouponIssueService {

    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final CustomCouponIssueRepository customCouponIssueRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void issue(long couponId, long userId) {
        Coupon coupon = findCouponWithLock(couponId);
        coupon.issue();
        saveCouponIssue(couponId, userId);
        publishCouponEvent(coupon);
    }

    public Coupon findCoupon(long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponIssueException(COUPON_NOT_EXIST));
    }

    @Transactional
    public Coupon findCouponWithLock(long couponId) {
        return couponRepository.findCouponWithLock(couponId)
                .orElseThrow(() -> new CouponIssueException(COUPON_NOT_EXIST));
    }

    @Transactional
    public CouponIssue saveCouponIssue(long couponId, long userId) {
        checkAlreadyIssuance(couponId, userId);
        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(couponId)
                .userId(userId)
                .build();

        return couponIssueRepository.save(couponIssue);
    }

    // 이미 쿠폰이 발급되었는지 확인
    private void checkAlreadyIssuance(long couponId, long userId) {
        CouponIssue issue = customCouponIssueRepository.findFirstCouponIssue(couponId, userId);
        if (issue != null) {
            throw new CouponIssueException(DUPLICATED_COUPON_ISSUE);
        }
    }

    private void publishCouponEvent(Coupon coupon) {
        // 쿠폰이 모두 발행된 경우
        if (coupon.isIssueComplete()) {
            applicationEventPublisher.publishEvent(new CouponIssueCompleteEvent(coupon.getId()));
        }
    }
}
