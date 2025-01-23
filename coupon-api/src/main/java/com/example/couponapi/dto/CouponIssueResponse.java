package com.example.couponapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
public record CouponIssueResponse(boolean isSuccess,String comment) {
}
