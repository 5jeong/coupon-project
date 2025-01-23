package com.example.couponapi.controller;

import com.example.couponapi.dto.CouponIssueRequest;
import com.example.couponapi.dto.CouponIssueResponse;
import com.example.couponapi.service.CouponIssueRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CouponIssueController {
    private final CouponIssueRequestService couponIssueRequestService;

    @PostMapping("/v1/issue")
    public CouponIssueResponse issueV1(@RequestBody CouponIssueRequest request) {
        couponIssueRequestService.issueRequestV1(request);
        return new CouponIssueResponse(true,null);
    }
}
