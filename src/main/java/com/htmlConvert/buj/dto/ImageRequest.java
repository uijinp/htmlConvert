package com.htmlConvert.buj.dto;

import java.util.Map;

// 클라이언트로부터 JSON 데이터를 받기 위한 클래스
public class ImageRequest {
    private Map<String, Object> data;

    // Getters and Setters
    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}