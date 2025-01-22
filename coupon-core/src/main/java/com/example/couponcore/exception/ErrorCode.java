package com.example.couponcore.exception;

import lombok.Getter;

public enum ErrorCode {

    INVALID_COUPON_ISSUE_QUANTITY("발급 가능한 수량을 초과 합니다."),
    INVALID_COUPON_ISSUE_DATE("발급 가능한 일자가 아닙니다."),
    COUPON_NOT_EXIST("쿠폰이 존재하지 않습니다."),
    DUPLICATED_COUPON_ISSUE("쿠폰이 이미 발급 되었습니다.");
    final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
