package com.htmlConvert.buj.config;

import com.htmlConvert.buj.service.BrowserFactory;
import com.microsoft.playwright.Browser;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value; // Value 어노테이션 추가
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BrowserPoolConfig {

    private final BrowserFactory browserFactory;

    // application.properties에서 값 주입
    @Value("${browser.pool.max-total:5}")
    private int maxTotal;
    @Value("${browser.pool.max-idle:5}")
    private int maxIdle;
    @Value("${browser.pool.min-idle:0}")
    private int minIdle;
    @Value("${browser.pool.max-wait-millis:30000}")
    private long maxWaitMillis;
    @Value("${browser.pool.eviction-run-millis:30000}")
    private long evictionRunMillis;
    @Value("${browser.pool.time-between-eviction-runs-millis:30000}")
    private long timeBetweenEvictionRunsMillis;
    @Value("${browser.pool.min-evictable-idle-time-millis:600000}")
    private long minEvictableIdleTimeMillis;


    public BrowserPoolConfig(BrowserFactory browserFactory) {
        this.browserFactory = browserFactory;
    }

    @Bean(destroyMethod = "close")
    public GenericObjectPool<Browser> browserPool() {
        GenericObjectPoolConfig<Browser> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setMaxWaitMillis(maxWaitMillis); // 대기 시간 설정 추가

        // Eviction 관련 설정 추가
        config.setEvictionPolicyClassName("org.apache.commons.pool2.impl.DefaultEvictionPolicy"); // 기본 Eviction Policy 사용
        config.setSoftMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis); // 최소 유휴 시간 설정
        config.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis); // Eviction 주기 설정
        config.setNumTestsPerEvictionRun(maxTotal); // 한 번에 검사할 객체 수 (maxTotal로 설정하여 모든 객체 검사)


        config.setTestOnBorrow(true); // 대여 시 유효성 검사
        config.setTestOnReturn(true); // 반납 시 유효성 검사

        GenericObjectPool<Browser> pool = new GenericObjectPool<>(browserFactory, config);
        // 애플리케이션 종료 시 풀이 안전하게 닫히도록 셧다운 훅 추가
        Runtime.getRuntime().addShutdownHook(new Thread(pool::close));
        return pool;
    }
}
