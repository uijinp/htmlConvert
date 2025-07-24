package com.htmlConvert.buj.service;

import com.fasterxml.jackson.databind.ObjectMapper; // JSON 처리를 위해 추가
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.microsoft.playwright.*;
import org.springframework.core.io.ClassPathResource; // 클래스패스 리소스 로딩을 위해 추가
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageGenerationService {

    private final MustacheFactory mustacheFactory;
    private final ObjectMapper objectMapper; // JSON 문자열 변환을 위한 ObjectMapper 주입
    private final Playwright playwright;

    // 생성자에서 Playwright와 ObjectMapper를 주입받도록 수정
    public ImageGenerationService(Playwright playwright, ObjectMapper objectMapper) {
        this.mustacheFactory = new DefaultMustacheFactory("templates");
        this.playwright = playwright;
        this.objectMapper = objectMapper;
    }

    public byte[] generateImageFromTemplate(String templateName, Map<String, Object> data) {
        // 1. 템플릿 이름에 따라 다른 방식으로 HTML 컨텐츠 생성
        String htmlContent;
        try {
            // 템플릿 파일을 읽어옴
            String templateString = new String(new ClassPathResource("templates/" + templateName + ".html").getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            // 템플릿 이름에 따라 분기 처리
            if ("report-templat1".equals(templateName)) { // 오타가 있는 파일 이름 그대로 사용
                htmlContent = injectDataIntoJavaScript(templateString, data);
            } else {
                // 기존 Mustache 방식
                htmlContent = compileWithMustache(templateString, data);
            }
        } catch (IOException e) {
            throw new RuntimeException("템플릿 파일을 읽거나 처리하는 중 오류 발생: " + templateName, e);
        }


        // 2. Playwright를 사용하여 HTML 렌더링 및 스크린샷 (성능 개선안 적용)
        try (Browser browser = this.playwright.chromium().launch()) {
            Page page = browser.newPage();
            page.setContent(htmlContent);
            byte[] imageBytes = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
            return imageBytes;
        }
    }

    /**
     * 새로운 방식: HTML 내의 JavaScript 변수에 JSON 데이터를 직접 주입합니다.
     */
    private String injectDataIntoJavaScript(String template, Map<String, Object> data) throws IOException {
        // Map 데이터를 JSON 문자열로 변환
        String jsonString = objectMapper.writeValueAsString(data);
        // HTML의 'const data = {};' 부분을 'const data = {...};' 로 교체
        return template.replace("const data = {};", "const data = " + jsonString + ";");
    }

    /**
     * 기존 방식: Mustache 템플릿을 컴파일합니다.
     */
    private String compileWithMustache(String template, Map<String, Object> data) throws IOException {
        Mustache mustache = mustacheFactory.compile(new java.io.StringReader(template), "template");
        Writer writer = new StringWriter();
        mustache.execute(writer, Map.of("data", data)).flush();
        return writer.toString();
    }

    /**
     * 이미지 byte array를 파일로 저장하고 접근 URL을 반환합니다. (변경 없음)
     */
    public String saveImageAndGetUrl(byte[] imageBytes) throws IOException {
        Path directory = Paths.get("generated-images");
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        String filename = UUID.randomUUID().toString() + ".png";
        Path filePath = directory.resolve(filename);
        Files.write(filePath, imageBytes);
        return "/images/generated/" + filename;
    }
}