package com.example.couponcore.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CouponIssueException extends RuntimeException {

    private final ErrorCode errorCode;

}
