package com.example.couponcore.model;

import static org.junit.jupiter.api.Assertions.*;

import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CouponTest {

    @DisplayName("발급수량이 남아있으면 true 반환")
    @Test
    void validateIssueQuantity_1() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .build();
        //when
        boolean result = coupon.validateIssueQuantity();
        //then
        Assertions.assertTrue(result);
    }

    @DisplayName("발급수량이 남아있지 않으면 false 반환")
    @Test
    void validateIssueQuantity_2() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .build();
        //when
        boolean result = coupon.validateIssueQuantity();
        //then
        Assertions.assertFalse(result);
    }

    @DisplayName("최대 발급 수량이 설정되지 않으면 true 반환")
    @Test
    void validateIssueQuantity_3() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(null)
                .issuedQuantity(100)
                .build();
        //when
        boolean result = coupon.validateIssueQuantity();
        //then
        Assertions.assertTrue(result);
    }

    @DisplayName("쿠폰 발급 기간이 시작되지 않을때 false 반환")
    @Test
    void validateIssueDate_1() {
        //given : 아직 쿠폰발급x
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        //when
        boolean result = coupon.validateIssueDate();
        //then
        Assertions.assertFalse(result);
    }

    @DisplayName("쿠폰 발급 기간에 해당하면 true 반환")
    @Test
    void validateIssueDate_2() {
        //given : 아직 쿠폰발급x
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();
        //when
        boolean result = coupon.validateIssueDate();
        //then
        Assertions.assertTrue(result);
    }

    @DisplayName("쿠폰 만료기간이 지나면 false 반환")
    @Test
    void validateIssueDate_3() {
        //given : 아직 쿠폰발급x
        Coupon coupon = Coupon.builder()
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();
        //when
        boolean result = coupon.validateIssueDate();
        //then
        Assertions.assertFalse(result);
    }

    @DisplayName("쿠폰 발급기간이 유효하다면 발급에 성공 ")
    @Test
    void issue_1() {
        //given : 아직 쿠폰발급x
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        //when
        coupon.issue();
        //then
        assertEquals(coupon.getIssuedQuantity(), 100);
    }

    @DisplayName("쿠폰 발급 수량을 초과하면 예외반환 ")
    @Test
    void issue_2() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();
        //when & then
        CouponIssueException exception = assertThrows(CouponIssueException.class, coupon::issue);
        assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
    }

    @DisplayName("쿠폰 발급기간이 유효하지 않으면 예외반환  ")
    @Test
    void issue_3() {
        //given : 아직 쿠폰발급x
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(99)
                .dateIssueStart(LocalDateTime.now().plusDays(1))
                .dateIssueEnd(LocalDateTime.now().plusDays(2))
                .build();

        //when & then
        CouponIssueException exception = assertThrows(CouponIssueException.class, coupon::issue);
        assertEquals(exception.getErrorCode(), ErrorCode.INVALID_COUPON_ISSUE_DATE);
    }

    @DisplayName("쿠폰 발급기간이 종료되면 true 반환 ")
    @Test
    void isIssueComplete_1() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().minusDays(1))
                .build();

        //when & then
        boolean result = coupon.isIssueComplete();
        assertTrue(result);
    }

    @DisplayName("잔여 쿠폰 발급 수량이 없다면 true 반환 ")
    @Test
    void isIssueComplete_2() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(100)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();

        //when & then
        boolean result = coupon.isIssueComplete();
        assertTrue(result);
    }

    @DisplayName("쿠폰 발급기간과 수량이 유효하면 false 반환 ")
    @Test
    void isIssueComplete_3() {
        //given
        Coupon coupon = Coupon.builder()
                .totalQuantity(100)
                .issuedQuantity(0)
                .dateIssueStart(LocalDateTime.now().minusDays(2))
                .dateIssueEnd(LocalDateTime.now().plusDays(1))
                .build();

        //when & then
        boolean result = coupon.isIssueComplete();
        assertFalse(result);
    }

}