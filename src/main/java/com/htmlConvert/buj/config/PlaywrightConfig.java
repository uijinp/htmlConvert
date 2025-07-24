package com.htmlConvert.buj.config;

import com.microsoft.playwright.Playwright;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlaywrightConfig {

    /**
     * Playwright 객체를 Spring Bean으로 등록합니다.
     * 이제 Spring은 이 메서드를 통해 Playwright 인스턴스를 생성하고 관리합니다.
     *
     * @Bean(destroyMethod = "close") : 애플리케이션이 종료될 때,
     *                                 Playwright 리소스가 안전하게 해제되도록
     *                                 자동으로 close() 메서드를 호출해주는 매우 중요한 설정입니다.
     */
    @Bean(destroyMethod = "close")
    public Playwright playwright() {
        return Playwright.create();
    }
}
    