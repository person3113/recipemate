package com.recipemate.global.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 이미지 업로드 유틸리티
 * 현재는 placeholder URL 생성 (추후 실제 파일 저장 로직으로 교체)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageUploadUtil {

    private final ImageOptimizationService imageOptimizationService;

    private static final int MAX_IMAGE_COUNT = 3;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    /**
     * 이미지 파일 목록 업로드
     * @param imageFiles 업로드할 이미지 파일 목록
     * @return 업로드된 이미지 URL 목록
     */
    public List<String> uploadImages(List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return new ArrayList<>();
        }

        validateImageCount(imageFiles);

        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : imageFiles) {
            if (file.isEmpty()) {
                continue;
            }
            validateImageFile(file);
            String imageUrl = uploadSingleImage(file);
            imageUrls.add(imageUrl);
        }

        return imageUrls;
    }

    /**
     * 단일 이미지 업로드
     * 이미지 최적화 후 저장
     */
    private String uploadSingleImage(MultipartFile file) {
        try {
            // 이미지 최적화
            byte[] optimizedImageData = imageOptimizationService.optimizeImage(
                    file.getBytes(),
                    file.getOriginalFilename()
            );
            
            // TODO: 실제 파일 저장 로직 구현 (S3, 로컬 스토리지 등)
            // 최적화된 이미지 데이터를 저장해야 함
            String fileName = generateFileName(file.getOriginalFilename());
            
            log.info("이미지 업로드 완료: {} (원본: {} bytes, 최적화: {} bytes)", 
                    fileName, file.getSize(), optimizedImageData.length);
            
            return "/images/" + fileName;
            
        } catch (IOException e) {
            log.error("이미지 업로드 실패: {}", file.getOriginalFilename(), e);
            throw new IllegalArgumentException("이미지 업로드에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 이미지 파일 개수 검증
     */
    private void validateImageCount(List<MultipartFile> imageFiles) {
        long nonEmptyFileCount = imageFiles.stream()
            .filter(file -> !file.isEmpty())
            .count();
        
        if (nonEmptyFileCount > MAX_IMAGE_COUNT) {
            throw new IllegalArgumentException("이미지는 최대 " + MAX_IMAGE_COUNT + "장까지 업로드 가능합니다");
        }
    }

    /**
     * 이미지 파일 유효성 검증
     */
    private void validateImageFile(MultipartFile file) {
        // 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다");
        }

        // 파일 형식 검증
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. (지원: JPEG, PNG, GIF, WebP)");
        }
    }

    /**
     * 고유한 파일명 생성
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}
