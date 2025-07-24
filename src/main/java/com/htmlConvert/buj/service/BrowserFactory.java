package com.htmlConvert.buj.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.springframework.stereotype.Component;

@Component
public class BrowserFactory extends BasePooledObjectFactory<Browser> {

    private final Playwright playwright;

    public BrowserFactory(Playwright playwright) {
        this.playwright = playwright;
    }

    @Override
    public Browser create() throws Exception {
        return playwright.chromium().launch();
    }

    @Override
    public PooledObject<Browser> wrap(Browser browser) {
        return new DefaultPooledObject<>(browser);
    }

    @Override
    public void destroyObject(PooledObject<Browser> p) throws Exception {
        Browser browser = p.getObject();
        if (browser != null && browser.isConnected()) {
            browser.close();
        }
    }

    @Override
    public boolean validateObject(PooledObject<Browser> p) {
        return p.getObject() != null && p.getObject().isConnected();
    }
}
