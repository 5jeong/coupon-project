package com.example.couponcore.model;

import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "coupons")
public class Coupon extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    CouponType couponType;

    private Integer totalQuantity;

    @Column(nullable = false)
    private int issuedQuantity;

    @Column(nullable = false)
    private int discountAmount;

    @Column(nullable = false)
    private int minAvailableAmount;

    @Column(nullable = false)
    private LocalDateTime dateIssueStart; // 쿠폰 발급 시간

    @Column(nullable = false)
    private LocalDateTime dateIssueEnd;


    public boolean validateIssueQuantity(){
        // 수량이 없는 쿠폰인 경우
        if(totalQuantity == null){
            return true;
        }
        return totalQuantity > issuedQuantity;
    }

    public boolean validateIssueDate(){
        LocalDateTime now = LocalDateTime.now();
        return dateIssueStart.isBefore(now) && dateIssueEnd.isAfter(now);
    }

    // 쿠폰 발급
    public void issue(){
        if(!validateIssueQuantity()){
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY);
        }
        if(!validateIssueDate()){
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_DATE);
        }

        issuedQuantity++;
    }
}
