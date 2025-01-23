package com.example.couponapi;

import com.example.couponapi.dto.CouponIssueResponse;
import com.example.couponcore.exception.CouponIssueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CouponControllerAdvice {

    @ExceptionHandler(CouponIssueException.class)
    public CouponIssueResponse couponIssueExceptionHandler(CouponIssueException exception) {
        return new CouponIssueResponse(false, exception.getErrorCode().message);
    }
}
