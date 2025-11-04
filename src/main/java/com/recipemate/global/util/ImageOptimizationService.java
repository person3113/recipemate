package com.recipemate.global.util;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 이미지 최적화 서비스
 * 
 * - 이미지 크기 조정 (리사이즈)
 * - 이미지 압축 (품질 조정)
 * - 썸네일 생성
 * - WebP 형식 변환
 */
@Slf4j
@Service
public class ImageOptimizationService {

    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1920;
    private static final double COMPRESSION_QUALITY = 0.85; // 85% 품질
    private static final double THUMBNAIL_QUALITY = 0.8; // 썸네일 80% 품질

    /**
     * 이미지를 최적화합니다.
     * - 큰 이미지는 최대 크기(1920x1920)로 리사이즈
     * - 파일 크기를 줄이기 위해 압축 적용
     * 
     * @param imageData 원본 이미지 데이터
     * @param fileName 파일명 (확장자 판별용)
     * @return 최적화된 이미지 데이터
     * @throws IllegalArgumentException 잘못된 입력값
     */
    public byte[] optimizeImage(byte[] imageData, String fileName) {
        validateInput(imageData, fileName);

        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
            if (originalImage == null) {
                throw new IllegalArgumentException("유효하지 않은 이미지 데이터입니다.");
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            String format = getImageFormat(fileName);

            // 큰 이미지는 리사이즈, 작은 이미지는 압축만 적용
            if (originalWidth > MAX_WIDTH || originalHeight > MAX_HEIGHT) {
                log.debug("이미지 리사이즈: {}x{} -> 최대 {}x{}", 
                        originalWidth, originalHeight, MAX_WIDTH, MAX_HEIGHT);
                
                Thumbnails.of(originalImage)
                        .size(MAX_WIDTH, MAX_HEIGHT)
                        .outputQuality(COMPRESSION_QUALITY)
                        .outputFormat(format)
                        .toOutputStream(outputStream);
            } else {
                log.debug("이미지 압축만 적용: {}x{}", originalWidth, originalHeight);
                
                Thumbnails.of(originalImage)
                        .scale(1.0) // 크기 유지
                        .outputQuality(COMPRESSION_QUALITY)
                        .outputFormat(format)
                        .toOutputStream(outputStream);
            }

            byte[] optimizedData = outputStream.toByteArray();
            log.info("이미지 최적화 완료: {} -> {} bytes ({}% 감소)",
                    imageData.length, optimizedData.length,
                    String.format("%.1f", (1 - (double) optimizedData.length / imageData.length) * 100));

            return optimizedData;

        } catch (IOException e) {
            log.error("이미지 최적화 실패: {}", fileName, e);
            throw new IllegalArgumentException("이미지 최적화 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 썸네일 이미지를 생성합니다.
     * 비율을 유지하면서 지정된 크기 이내로 축소합니다.
     * 
     * @param imageData 원본 이미지 데이터
     * @param width 최대 너비
     * @param height 최대 높이
     * @return 썸네일 이미지 데이터
     */
    public byte[] createThumbnail(byte[] imageData, int width, int height) {
        if (imageData == null || imageData.length == 0) {
            throw new IllegalArgumentException("이미지 데이터가 비어있습니다.");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("썸네일 크기는 0보다 커야 합니다.");
        }

        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
            if (originalImage == null) {
                throw new IllegalArgumentException("유효하지 않은 이미지 데이터입니다.");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Thumbnails.of(originalImage)
                    .size(width, height)
                    .outputQuality(THUMBNAIL_QUALITY)
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);

            byte[] thumbnailData = outputStream.toByteArray();
            log.debug("썸네일 생성 완료: {}x{} -> {} bytes", width, height, thumbnailData.length);

            return thumbnailData;

        } catch (IOException e) {
            log.error("썸네일 생성 실패", e);
            throw new IllegalArgumentException("썸네일 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 이미지를 WebP 형식으로 변환합니다.
     * WebP는 JPEG/PNG 대비 더 나은 압축률을 제공합니다.
     * 
     * 참고: Java 기본 ImageIO는 WebP를 지원하지 않으므로,
     * 현재는 고품질 JPEG로 변환하는 방식으로 구현합니다.
     * 실제 WebP 변환이 필요한 경우 별도 라이브러리(webp-imageio) 추가 필요.
     * 
     * @param imageData 원본 이미지 데이터
     * @return 변환된 이미지 데이터
     */
    public byte[] convertToWebP(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            throw new IllegalArgumentException("이미지 데이터가 비어있습니다.");
        }

        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
            if (originalImage == null) {
                throw new IllegalArgumentException("유효하지 않은 이미지 데이터입니다.");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // WebP 라이브러리가 없으므로 고품질 JPEG로 변환
            // TODO: webp-imageio 라이브러리 추가 시 실제 WebP 변환 구현
            Thumbnails.of(originalImage)
                    .scale(1.0)
                    .outputQuality(0.9) // 높은 품질 유지
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);

            byte[] convertedData = outputStream.toByteArray();
            log.debug("이미지 형식 변환 완료: {} bytes -> {} bytes",
                    imageData.length, convertedData.length);

            return convertedData;

        } catch (IOException e) {
            log.error("WebP 변환 실패", e);
            throw new IllegalArgumentException("이미지 변환 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 입력값을 검증합니다.
     */
    private void validateInput(byte[] imageData, String fileName) {
        if (imageData == null || imageData.length == 0) {
            throw new IllegalArgumentException("이미지 데이터가 비어있습니다.");
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("파일명이 비어있습니다.");
        }
    }

    /**
     * 파일명에서 이미지 포맷을 추출합니다.
     */
    private String getImageFormat(String fileName) {
        String lowerFileName = fileName.toLowerCase();
        if (lowerFileName.endsWith(".png")) {
            return "png";
        } else if (lowerFileName.endsWith(".gif")) {
            return "gif";
        }
        return "jpg"; // 기본값은 JPEG
    }
}
