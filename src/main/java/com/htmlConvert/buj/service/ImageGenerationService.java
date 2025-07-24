package com.htmlConvert.buj.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.microsoft.playwright.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value; // @Value 어노테이션 추가
import org.springframework.core.io.ClassPathResource;
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
    private static final Logger logger = LoggerFactory.getLogger(ImageGenerationService.class);

    private final MustacheFactory mustacheFactory;
    private final ObjectMapper objectMapper;
    private final Playwright playwright;
    @Value("${app.template.external-path:}") // 값이 없을 경우 빈 문자열이 되도록 기본값 설정
    private String externalTemplatePath;

    public ImageGenerationService(Playwright playwright, ObjectMapper objectMapper) {
        this.mustacheFactory = new DefaultMustacheFactory("templates");
        this.playwright = playwright;
        this.objectMapper = objectMapper;
    }

    public byte[] generateImageFromTemplate(String templateName, Map<String, Object> data) {
        String htmlContent;
        try {
            // 템플릿 내용을 가져오는 로직을 별도 메소드로 분리
            String templateString = getTemplateContent(templateName);
            htmlContent = injectDataIntoJavaScript(templateString, data);

        } catch (IOException e) {
            throw new RuntimeException("템플릿 파일을 읽거나 처리하는 중 오류 발생: " + templateName, e);
        }

        try (Browser browser = this.playwright.chromium().launch()) {
            Page page = browser.newPage();
            page.setContent(htmlContent);
            return page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
        }
    }

    /**
     * 외부 경로 또는 내부 클래스패스에서 템플릿 내용을 읽어옵니다.
     * 외부 경로에 파일이 존재하면 그 파일을 우선적으로 사용합니다.
     */
    private String getTemplateContent(String templateName) throws IOException {
        // 1. 외부 경로 확인
        // externalTemplatePath가 비어있지 않은 경우에만 외부 경로를 탐색합니다.
        if (externalTemplatePath != null && !externalTemplatePath.isBlank()) {
            Path externalPath = Paths.get(externalTemplatePath, templateName + ".html");
            if (Files.exists(externalPath)) {
                logger.info("외부 템플릿 파일을 사용합니다: {}", externalPath);
                return Files.readString(externalPath, StandardCharsets.UTF_8);
            }
        }

        // 2. 외부 경로에 파일이 없으면 내부(classpath) 템플릿 사용 (기존 로직)
        logger.info("내부 기본 템플릿 파일을 사용합니다: {}", templateName);
        ClassPathResource resource = new ClassPathResource("templates/" + templateName + ".html");
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
    /**
     * 새로운 방식: HTML 내의 JavaScript 변수에 JSON 데이터를 직접 주입합니다.
     */
    private String injectDataIntoJavaScript(String template, Map<String, Object> data) throws IOException {
        String jsonString = objectMapper.writeValueAsString(data);
        // 정규식을 사용하여 좀 더 안정적으로 교체
        return template.replaceFirst("const\\s+data\\s*=\\s*\\{\\s*\\};", "const data = " + jsonString + ";");
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