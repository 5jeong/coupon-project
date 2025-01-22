package com.example.couponcore.repository.mysql;

import static com.example.couponcore.model.QCouponIssue.couponIssue;

import com.example.couponcore.model.CouponIssue;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CustomCouponIssueRepository {

    private final JPAQueryFactory queryFactory;

    public CouponIssue findFirstCouponIssue(long couponId, long userId) {
        return queryFactory.selectFrom(couponIssue)
                .where(couponIssue.couponId.eq(couponId), couponIssue.userId.eq(userId))
                .fetchFirst();

    }
}
