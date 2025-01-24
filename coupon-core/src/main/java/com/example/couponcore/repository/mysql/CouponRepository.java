package com.example.couponcore.repository.mysql;

import com.example.couponcore.model.Coupon;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponRepository extends JpaRepository<Coupon,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c from Coupon c where c.id=:id")
    Optional<Coupon> findCouponWithLock(@Param("id") long id);
}
