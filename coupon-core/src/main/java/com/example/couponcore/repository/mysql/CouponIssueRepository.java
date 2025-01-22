package com.example.couponcore.repository.mysql;

import com.example.couponcore.model.CouponIssue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponIssueRepository extends JpaRepository<CouponIssue,Long> {
}
