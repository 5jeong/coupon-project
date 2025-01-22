package com.example.couponcore.service;

import static com.example.couponcore.exception.ErrorCode.COUPON_NOT_EXIST;
import static com.example.couponcore.exception.ErrorCode.DUPLICATED_COUPON_ISSUE;
import static com.example.couponcore.model.CouponType.FIRST_COME_FIRST_SERVED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.couponcore.TestConfig;
import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import com.example.couponcore.model.Coupon;
import com.example.couponcore.model.CouponIssue;
import com.example.couponcore.repository.mysql.CouponIssueRepository;
import com.example.couponcore.repository.mysql.CouponRepository;
import com.example.couponcore.repository.mysql.CustomCouponIssueRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CouponIssueServiceTest extends TestConfig {

    @Autowired
    CouponIssueService couponIssueService;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    CouponIssueRepository couponIssueRepository;

    @Autowired
    CustomCouponIssueRepository customCouponIssueRepository;

    @BeforeEach
    void clean() {
        couponRepository.deleteAllInBatch();
        couponIssueRepository.deleteAllInBatch();
    }

    @DisplayName("쿠폰 발급 내역이 존재하면 예외를 반환한다.")
    @Test
    void saveCouponIssue_1() {
        //given
        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(1L)
                .userId(1L)
                .build();

        couponIssueRepository.save(couponIssue);
        //when & then
        CouponIssueException exception = assertThrows(CouponIssueException.class,
                () -> couponIssueService.saveCouponIssue(couponIssue.getCouponId(), couponIssue.getUserId()));
        Assertions.assertEquals(exception.getErrorCode(), DUPLICATED_COUPON_ISSUE);
    }

    @DisplayName("쿠폰 발급 내역이 존재하지 않으면 쿠폰을 발급.")
    @Test
    void saveCouponIssue_2() {
        //given
        long couponId = 1L;
        long userId = 1L;

        //when & then
        CouponIssue couponIssue = couponIssueService.saveCouponIssue(couponId, userId);
        Assertions.assertTrue(couponIssueRepository.findById(couponIssue.getId()).isPresent());
    }

    @DisplayName("발급 수량, 중복 발급 문제가 없으면 쿠폰 발급")
    @Test
    void issue_1() {
        //given
        long userId = 1L;
        Coupon coupon = Coupon.builder().couponType(FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰1")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        couponRepository.save(coupon);

        //when
        couponIssueService.issue(coupon.getId(), userId);
        //then
        Coupon couponResult = couponRepository.findById(coupon.getId()).get();
        // 발급 수량 증가 검증
        Assertions.assertEquals(couponResult.getIssuedQuantity(), 1);

        CouponIssue couponIssue = customCouponIssueRepository.findFirstCouponIssue(couponResult.getId(), userId);
        assertNotNull(couponIssue);
    }

    @DisplayName("발급 수량에 문제가 있을때 예외 반환")
    @Test
    void issue_2() {
        //given
        long userId = 1L;
        Coupon coupon = Coupon.builder().couponType(FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰1")
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        couponRepository.save(coupon);

        //when & then
        CouponIssueException exception = assertThrows(CouponIssueException.class,
                () -> couponIssueService.issue(coupon.getId(), userId));
        assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
    }

    @DisplayName("발급 기한에 문제가 있을때 예외 반환")
    @Test
    void issue_3() {
        //given
        long userId = 1L;
        Coupon coupon = Coupon.builder().couponType(FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰1")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        couponRepository.save(coupon);

        //when & then
        CouponIssueException exception = assertThrows(CouponIssueException.class,
                () -> couponIssueService.issue(coupon.getId(), userId));

        assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_DATE);
    }


    @DisplayName("중복발급 문제가 있을때 예외 반환")
    @Test
    void issue_4() {
        //given
        long userId = 1L;
        Coupon coupon = Coupon.builder().couponType(FIRST_COME_FIRST_SERVED)
                .title("선착순 쿠폰1")
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        couponRepository.save(coupon);

        CouponIssue couponIssue = CouponIssue.builder()
                .couponId(coupon.getId())
                .userId(userId)
                .build();

        couponIssueRepository.save(couponIssue);

        //when & then
        CouponIssueException exception = assertThrows(CouponIssueException.class,
                () -> couponIssueService.issue(coupon.getId(), userId));

        assertEquals(exception.getErrorCode(), DUPLICATED_COUPON_ISSUE);
    }

    @DisplayName("쿠폰이 존재하지 않으면 예외 반환")
    @Test
    void issue_5() {
        //given
        long userId = 1L;
        long invalidCouponId = 1L;

        //when & then
        CouponIssueException exception = assertThrows(CouponIssueException.class,
                () -> couponIssueService.issue(invalidCouponId, userId));

        assertEquals(exception.getErrorCode(), COUPON_NOT_EXIST);
    }
}