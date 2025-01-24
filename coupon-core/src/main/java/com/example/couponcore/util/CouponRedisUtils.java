package com.example.couponcore.util;

public class CouponRedisUtils {

    private static final String ISSUE_REQUEST_KEY="issue.request.couponId=%s";
    private static final String ISSUE_REQUEST_QUEUE_KEY="issue.request.queue";
    private static final String REDIS_LOCK_NAME="lock_%s";


    public static String getIssueRequestKey(long couponId){
        return ISSUE_REQUEST_KEY.formatted(couponId);
    }

    public static String getIssueRequestQueueKey(){
        return ISSUE_REQUEST_QUEUE_KEY;
    }

    public static String getRedisLockName(long couponId){
        return REDIS_LOCK_NAME.formatted(couponId);
    }
}
