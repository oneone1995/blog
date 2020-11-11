package com.github.oneone1995.demo.redisson.rratelimiter;

import org.redisson.Redisson;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.CountDownLatch;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        RRateLimiter rateLimiter = createLimiter();


        int allThreadNum = 20;

        CountDownLatch latch = new CountDownLatch(allThreadNum);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < allThreadNum; i++) {
            new Thread(() -> {
                rateLimiter.acquire(1);
                System.out.println(Thread.currentThread().getName());
                latch.countDown();
            }).start();
        }
        latch.await();
        System.out.println("Elapsed " + (System.currentTimeMillis() - startTime));
    }

    public static RRateLimiter createLimiter() {
        Config config = new Config();
        config.useSingleServer()
                .setTimeout(1000000)
                .setAddress("redis://127.0.0.1:6379");

        RedissonClient redisson = Redisson.create(config);
        RRateLimiter rateLimiter = redisson.getRateLimiter("myRateLimiter");
        // 初始化
        // 最大流速 = 每1秒钟产生1个令牌
        rateLimiter.trySetRate(RateType.OVERALL, 3, 1, RateIntervalUnit.SECONDS);
        return rateLimiter;
    }
}
