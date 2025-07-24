package com.htmlConvert.buj.controller;

import com.htmlConvert.buj.dto.ImageRequest;
import com.htmlConvert.buj.service.ImageGenerationService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController // 이 어노테이션이 외부 요청을 받는 클래스임을 선언합니다.
@RequestMapping("/api/images") // 이 클래스의 모든 URL은 /api/images 로 시작합니다.
public class ImageGenerationController {

    private final ImageGenerationService imageGenerationService;

    public ImageGenerationController(ImageGenerationService imageGenerationService) {
        this.imageGenerationService = imageGenerationService;
    }

    /**
     * URL: POST /api/images/{templateName}/url
     */
    @PostMapping("/{templateName}/url")
    public ResponseEntity<Map<String, String>> generateImageAndGetUrl(
            @PathVariable String templateName,
            @RequestBody ImageRequest request) throws IOException {

        byte[] imageBytes = imageGenerationService.generateImageFromTemplate(templateName, request.getData());
        String imageUrl = imageGenerationService.saveImageAndGetUrl(imageBytes);

        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    /**
     * URL: POST /api/images/{templateName}/file
     */
    @PostMapping("/{templateName}/file")
    public ResponseEntity<Resource> generateImageAndGetFile(
            @PathVariable String templateName,
            @RequestBody ImageRequest request) {

        byte[] imageBytes = imageGenerationService.generateImageFromTemplate(templateName, request.getData());
        ByteArrayResource resource = new ByteArrayResource(imageBytes);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"generated-image.png\"")
                .body(resource);
    }
}