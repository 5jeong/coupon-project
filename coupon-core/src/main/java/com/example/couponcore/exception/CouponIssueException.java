package com.example.couponcore.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CouponIssueException extends RuntimeException {

    private final ErrorCode errorCode;

    @Override
    public String getMessage() {
        return "[%s] %s".formatted(errorCode, errorCode.message);
    }
}
