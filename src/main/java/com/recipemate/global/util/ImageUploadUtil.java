package com.recipemate.global.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 이미지 업로드 유틸리티 (Cloudinary 사용 + 병렬 업로드)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageUploadUtil {

    private final Cloudinary cloudinary;
    private final ExecutorService executorService = Executors.newFixedThreadPool(3); // 최대 3개 이미지 동시 업로드

    private static final int MAX_IMAGE_COUNT = 3;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
        "image/jpeg", "image/jpg", "image/png"
    );
    private static final int UPLOAD_TIMEOUT_SECONDS = 30; // 개별 업로드 타임아웃

    /**
     * 이미지 파일 목록 병렬 업로드
     * @param imageFiles 업로드할 이미지 파일 목록
     * @return 업로드된 이미지 URL 목록
     */
    public List<String> uploadImages(List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return new ArrayList<>();
        }

        validateImageCount(imageFiles);

        // 병렬 업로드를 위한 Future 리스트
        List<CompletableFuture<String>> futures = new ArrayList<>();
        int maxImages = Math.min(imageFiles.size(), MAX_IMAGE_COUNT);
        
        for (int i = 0; i < maxImages; i++) {
            MultipartFile file = imageFiles.get(i);
            
            // 빈 파일 건너뛰기
            if (file.isEmpty()) {
                log.warn("Empty file at index {}, skipping", i);
                continue;
            }
            
            // 검증
            try {
                validateImageFile(file);
            } catch (Exception e) {
                log.error("Validation failed for file at index {}: {}", i, e.getMessage());
                continue;
            }
            
            // 병렬 업로드 작업 생성
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return uploadSingleImage(file);
                } catch (IOException e) {
                    log.error("Failed to upload image {}: {}", file.getOriginalFilename(), e.getMessage());
                    return null;
                }
            }, executorService);
            
            futures.add(future);
        }
        
        // 모든 업로드 작업 완료 대기
        List<String> imageUrls = futures.stream()
            .map(future -> {
                try {
                    return future.get(UPLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    log.error("Image upload timeout after {} seconds", UPLOAD_TIMEOUT_SECONDS);
                    return null;
                } catch (Exception e) {
                    log.error("Image upload failed: {}", e.getMessage());
                    return null;
                }
            })
            .filter(url -> url != null) // null 제거 (실패한 업로드)
            .toList();
        
        log.info("Successfully uploaded {} out of {} images (parallel)", imageUrls.size(), maxImages);
        return imageUrls;
    }

    /**
     * 단일 이미지 업로드 (Cloudinary)
     */
    private String uploadSingleImage(MultipartFile file) throws IOException {
        long startTime = System.currentTimeMillis();
        
        // 1. Cloudinary 업로드 옵션 설정
        Map<String, Object> uploadParams = ObjectUtils.asMap(
            "folder", "recipemate/group-purchases",      // 저장 폴더
            "resource_type", "image",                    // 리소스 타입
            "transformation", new Transformation()       // 이미지 변환 설정
                .width(800)                              // 최대 너비
                .height(600)                             // 최대 높이
                .crop("limit")                           // 비율 유지하며 크기 제한
                .quality("auto")                         // 자동 품질 최적화
                .fetchFormat("auto")                     // 브라우저에 따라 WebP/JPEG 자동 선택
        );
        
        // 2. Cloudinary에 업로드
        log.info("Uploading image to Cloudinary: {}", file.getOriginalFilename());
        Map uploadResult = cloudinary.uploader().upload(
            file.getBytes(), 
            uploadParams
        );
        
        // 3. HTTPS URL 반환
        String imageUrl = uploadResult.get("secure_url").toString();
        long elapsedTime = System.currentTimeMillis() - startTime;
        log.info("Image uploaded successfully in {}ms: {}", elapsedTime, imageUrl);
        
        return imageUrl;
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
            throw new IllegalArgumentException(
                String.format("이미지 크기는 5MB 이하여야 합니다 (현재: %.2fMB)", 
                    file.getSize() / (1024.0 * 1024.0))
            );
        }

        // 파일 형식 검증
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("JPG 또는 PNG 형식의 이미지만 업로드 가능합니다");
        }
    }
    
    /**
     * 애플리케이션 종료 시 스레드 풀 정리
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down ImageUploadUtil executor service");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                log.warn("Executor service did not terminate in 60 seconds, forcing shutdown");
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            log.error("Executor service shutdown interrupted", e);
        }
    }
}
