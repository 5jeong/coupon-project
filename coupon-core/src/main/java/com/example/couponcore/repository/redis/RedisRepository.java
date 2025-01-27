package com.example.couponcore.repository.redis;

import static com.example.couponcore.util.CouponRedisUtils.getIssueRequestKey;
import static com.example.couponcore.util.CouponRedisUtils.getIssueRequestQueueKey;

import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import com.example.couponcore.repository.redis.dto.CouponIssueRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<String> issueScript = issueRequestScript();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Long sAdd(String key, String value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    public Long sCard(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    public Boolean sIsMember(String key, String value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public Long rPush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    public void issueRequest(long couponId, long userId, int totalIssueQuantity) {
        String issueRequestKey = getIssueRequestKey(couponId);
        String issueRequestQueueKey = getIssueRequestQueueKey();

        CouponIssueRequest couponIssueRequest = new CouponIssueRequest(couponId, userId);
        try {
            String code = redisTemplate.execute(
                    issueScript,
                    List.of(issueRequestKey, issueRequestQueueKey), // KEYS
                    String.valueOf(userId), // ARGV[1] : 요청한 사용자ID
                    String.valueOf(totalIssueQuantity), // ARGV[2] : 발급 가능한 최대 쿠폰수량
                    objectMapper.writeValueAsString(couponIssueRequest) // ARGV[3] 직렬화된 요청객체(value)
            );
            CouponIssueRequestCode.checkRequestResult(CouponIssueRequestCode.findCode(code));
        } catch (JsonProcessingException e) {
            throw new CouponIssueException(ErrorCode.FAIL_COUPON_ISSUE_REQUES);
        }
    }

    /**
     * Redis에서 실행할 Lua 스크립트를 생성합니다. <br> 이 스크립트는 중복 요청 여부와 발급 가능 여부를 확인한 후 요청을 처리
     *
     * @return RedisScript 객체
     */
    private RedisScript<String> issueRequestScript() {
        String script = """
                if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then 
                    return '2'
                end
                            
                if tonumber(ARGV[2]) > redis.call('SCARD', KEYS[1]) then
                    redis.call('SADD', KEYS[1], ARGV[1])
                    redis.call('RPUSH', KEYS[2], ARGV[3])
                    return '1'
                end
                            
                return '3'
                """;
        return RedisScript.of(script, String.class);
    }

}
