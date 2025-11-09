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
     * 업로드 시에는 원본을 저장하고, 변환은 URL로 동적으로 처리 (CDN 캐시 활용)
     */
    private String uploadSingleImage(MultipartFile file) throws IOException {
        long startTime = System.currentTimeMillis();
        
        // 1. Cloudinary 업로드 옵션 설정 (변환 없이 원본 저장)
        Map<String, Object> uploadParams = ObjectUtils.asMap(
            "folder", "recipemate/group-purchases",      // 저장 폴더
            "resource_type", "image"                     // 리소스 타입
            // transformation 제거 - 필요할 때 URL로 동적 변환
        );
        
        // 2. Cloudinary에 업로드
        log.info("Uploading image to Cloudinary: {}", file.getOriginalFilename());
        Map uploadResult = cloudinary.uploader().upload(
            file.getBytes(), 
            uploadParams
        );
        
        // 3. 최적화된 이미지 URL 생성 (동적 변환)
        String baseUrl = uploadResult.get("secure_url").toString();
        String optimizedUrl = baseUrl.replace("/upload/", "/upload/w_800,h_600,c_limit,q_auto,f_auto/");
        
        long elapsedTime = System.currentTimeMillis() - startTime;
        log.info("Image uploaded successfully in {}ms: {}", elapsedTime, optimizedUrl);
        
        return optimizedUrl;
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
     * Cloudinary에서 이미지 삭제
     * @param imageUrls 삭제할 이미지 URL 목록
     */
    public void deleteImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        
        for (String imageUrl : imageUrls) {
            try {
                // URL에서 public_id 추출
                String publicId = extractPublicIdFromUrl(imageUrl);
                
                // Cloudinary에서 삭제
                Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Image deleted from Cloudinary: {} (result: {})", publicId, result.get("result"));
            } catch (Exception e) {
                log.error("Failed to delete image from Cloudinary: {}", imageUrl, e);
                // 삭제 실패해도 계속 진행 (DB에서는 제거)
            }
        }
    }
    
    /**
     * Cloudinary URL에서 public_id 추출
     * 예: https://res.cloudinary.com/dt9xgsr2z/image/upload/v1234567890/recipemate/group-purchases/abc123.jpg
     * -> recipemate/group-purchases/abc123
     */
    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            // "/upload/" 이후 부분 추출
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid Cloudinary URL: " + imageUrl);
            }
            
            String path = parts[1];
            
            // 변환 파라미터 제거 (w_800,h_600,c_limit,q_auto,f_auto/)
            if (path.contains("/")) {
                // 마지막 '/' 이후만 파일 경로
                int lastSlash = path.lastIndexOf('/');
                if (lastSlash > 0) {
                    // 변환 파라미터가 있는 경우: w_800,h_600.../recipemate/...
                    // 변환 파라미터 건너뛰기
                    String[] segments = path.split("/");
                    StringBuilder pathBuilder = new StringBuilder();
                    boolean foundFolder = false;
                    for (String segment : segments) {
                        if (segment.startsWith("recipemate") || foundFolder) {
                            if (pathBuilder.length() > 0) {
                                pathBuilder.append("/");
                            }
                            pathBuilder.append(segment);
                            foundFolder = true;
                        }
                    }
                    path = pathBuilder.toString();
                }
            }
            
            // 버전 정보(v1234567890) 제거
            path = path.replaceFirst("v\\d+/", "");
            
            // 확장자 제거
            int lastDot = path.lastIndexOf('.');
            if (lastDot > 0) {
                path = path.substring(0, lastDot);
            }
            
            return path;
        } catch (Exception e) {
            log.error("Failed to extract public_id from URL: {}", imageUrl, e);
            throw new IllegalArgumentException("Invalid Cloudinary URL: " + imageUrl, e);
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
