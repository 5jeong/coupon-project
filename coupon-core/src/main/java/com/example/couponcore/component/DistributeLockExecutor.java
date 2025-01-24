package com.example.couponcore.component;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class DistributeLockExecutor {
    private final RedissonClient redissonClient;

    /**
     * @param lockName
     * @param waitMilliSecond 락을 획득하기 위해 기다릴 최대 시간(초과되면 락 획득에 실패)
     * @param leaseMilliSecond 락이 유지되는 시간(지정된 시간이 지나면 자동해제)
     * @param logic 락이 획득된 상태에서 실행할 작업.
     */
    public void execute(String lockName, long waitMilliSecond, long leaseMilliSecond, Runnable logic) {
        RLock lock = redissonClient.getLock(lockName);
        try {
            boolean isLocked = lock.tryLock(waitMilliSecond, leaseMilliSecond, TimeUnit.MILLISECONDS);
            if(!isLocked){
                throw new IllegalArgumentException("["+lockName+"] lock 획득 실패" );
            }
            logic.run();
        } catch (InterruptedException e) {
            log.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }finally {
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }
}
