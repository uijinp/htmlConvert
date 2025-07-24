package com.htmlConvert.buj.config;

import com.htmlConvert.buj.service.BrowserFactory;
import com.microsoft.playwright.Browser;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BrowserPoolConfig {

    private final BrowserFactory browserFactory;

    public BrowserPoolConfig(BrowserFactory browserFactory) {
        this.browserFactory = browserFactory;
    }

    @Bean(destroyMethod = "close")
    public GenericObjectPool<Browser> browserPool() {
        GenericObjectPoolConfig<Browser> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(10); // 풀의 최대 브라우저 인스턴스 수
        config.setMaxIdle(5);   // 유휴 상태로 유지할 최대 브라우저 인스턴스 수
        config.setMinIdle(2);   // 유휴 상태로 유지할 최소 브라우저 인스턴스 수
        config.setTestOnBorrow(true); // 대여 시 유효성 검사
        config.setTestOnReturn(true); // 반납 시 유효성 검사

        GenericObjectPool<Browser> pool = new GenericObjectPool<>(browserFactory, config);
        // 애플리케이션 종료 시 풀이 안전하게 닫히도록 셧다운 훅 추가
        Runtime.getRuntime().addShutdownHook(new Thread(pool::close));
        return pool;
    }
}
