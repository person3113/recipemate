//package com.recipemate.global.util;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.io.TempDir;
//import org.springframework.mock.web.MockMultipartFile;
//
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.*;
//
//@DisplayName("이미지 업로드 통합 테스트")
//class ImageUploadIntegrationTest {
//
//    private ImageOptimizationService imageOptimizationService;
//    private ImageUploadUtil imageUploadUtil;
//
//    @TempDir
//    Path tempDir;
//
//    @BeforeEach
//    void setUp() {
//        imageOptimizationService = new ImageOptimizationService();
//        imageUploadUtil = new ImageUploadUtil(imageOptimizationService);
//    }
//
//    @Test
//    @DisplayName("이미지 업로드 시 자동으로 최적화가 적용된다")
//    void uploadImages_AutomaticallyOptimizes() throws IOException {
//        // Given: 큰 이미지 파일들
//        MockMultipartFile image1 = createMockImage(3000, 2000, "large1.jpg");
//        MockMultipartFile image2 = createMockImage(1920, 1080, "large2.jpg");
//        List<org.springframework.web.multipart.MultipartFile> images = List.of(image1, image2);
//
//        // When: 이미지 업로드 (내부적으로 최적화 수행)
//        List<String> imageUrls = imageUploadUtil.uploadImages(images);
//
//        // Then: 이미지 URL이 정상적으로 생성됨
//        assertThat(imageUrls).hasSize(2);
//        assertThat(imageUrls).allMatch(url -> url.startsWith("/images/"));
//        assertThat(imageUrls).allMatch(url -> url.endsWith(".jpg"));
//    }
//
//    @Test
//    @DisplayName("여러 형식의 이미지를 처리할 수 있다")
//    void uploadImages_HandlesMultipleFormats() throws IOException {
//        // Given: 다양한 형식의 이미지
//        MockMultipartFile jpegImage = createMockImage(1920, 1080, "image.jpg");
//        MockMultipartFile pngImage = createMockImage(1920, 1080, "image.png");
//        List<org.springframework.web.multipart.MultipartFile> images = List.of(jpegImage, pngImage);
//
//        // When: 업로드
//        List<String> imageUrls = imageUploadUtil.uploadImages(images);
//
//        // Then: 모두 정상 처리됨
//        assertThat(imageUrls).hasSize(2);
//        assertThat(imageUrls.get(0)).contains(".jpg");
//        assertThat(imageUrls.get(1)).contains(".png");
//    }
//
//    @Test
//    @DisplayName("빈 파일은 건너뛰고 처리한다")
//    void uploadImages_SkipsEmptyFiles() throws IOException {
//        // Given: 정상 파일과 빈 파일
//        MockMultipartFile validImage = createMockImage(1920, 1080, "valid.jpg");
//        MockMultipartFile emptyFile = new MockMultipartFile(
//                "file", "empty.jpg", "image/jpeg", new byte[0]
//        );
//        List<org.springframework.web.multipart.MultipartFile> images = List.of(validImage, emptyFile);
//
//        // When: 업로드
//        List<String> imageUrls = imageUploadUtil.uploadImages(images);
//
//        // Then: 빈 파일은 제외되고 1개만 업로드됨
//        assertThat(imageUrls).hasSize(1);
//    }
//
//    @Test
//    @DisplayName("최대 개수를 초과하면 예외가 발생한다")
//    void uploadImages_ThrowsExceptionWhenExceedsMaxCount() throws IOException {
//        // Given: 최대 개수(3개)를 초과하는 이미지
//        MockMultipartFile image1 = createMockImage(800, 600, "image1.jpg");
//        MockMultipartFile image2 = createMockImage(800, 600, "image2.jpg");
//        MockMultipartFile image3 = createMockImage(800, 600, "image3.jpg");
//        MockMultipartFile image4 = createMockImage(800, 600, "image4.jpg");
//        List<org.springframework.web.multipart.MultipartFile> images = List.of(image1, image2, image3, image4);
//
//        // When & Then: 예외 발생
//        assertThatThrownBy(() -> imageUploadUtil.uploadImages(images))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("최대 3장");
//    }
//
//    @Test
//    @DisplayName("최대 파일 크기를 초과하면 예외가 발생한다")
//    void uploadImages_ThrowsExceptionWhenExceedsMaxSize() {
//        // Given: 10MB를 초과하는 큰 파일
//        byte[] largeData = new byte[11 * 1024 * 1024]; // 11MB
//        MockMultipartFile largeFile = new MockMultipartFile(
//                "file", "large.jpg", "image/jpeg", largeData
//        );
//
//        // When & Then: 예외 발생
//        assertThatThrownBy(() -> imageUploadUtil.uploadImages(List.of(largeFile)))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("10MB");
//    }
//
//    @Test
//    @DisplayName("지원하지 않는 파일 형식은 예외가 발생한다")
//    void uploadImages_ThrowsExceptionForUnsupportedFormat() {
//        // Given: 지원하지 않는 형식
//        MockMultipartFile unsupportedFile = new MockMultipartFile(
//                "file", "document.pdf", "application/pdf", "fake content".getBytes()
//        );
//
//        // When & Then: 예외 발생
//        assertThatThrownBy(() -> imageUploadUtil.uploadImages(List.of(unsupportedFile)))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("지원하지 않는 이미지 형식");
//    }
//
//    @Test
//    @DisplayName("null 또는 빈 리스트는 빈 결과를 반환한다")
//    void uploadImages_ReturnsEmptyListForNullOrEmpty() {
//        // When & Then: null
//        List<String> result1 = imageUploadUtil.uploadImages(null);
//        assertThat(result1).isEmpty();
//
//        // When & Then: 빈 리스트
//        List<String> result2 = imageUploadUtil.uploadImages(List.of());
//        assertThat(result2).isEmpty();
//    }
//
//    /**
//     * 테스트용 Mock 이미지 생성
//     */
//    private MockMultipartFile createMockImage(int width, int height, String fileName) throws IOException {
//        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//
//        // 이미지에 패턴 추가
//        for (int y = 0; y < height; y++) {
//            for (int x = 0; x < width; x++) {
//                int rgb = ((x + y) % 256) << 16 | ((x * 2) % 256) << 8 | ((y * 2) % 256);
//                image.setRGB(x, y, rgb);
//            }
//        }
//
//        // 임시 파일로 저장 후 읽기
//        Path tempFile = tempDir.resolve(fileName);
//        String format = fileName.endsWith(".png") ? "png" : "jpg";
//        ImageIO.write(image, format, tempFile.toFile());
//        byte[] imageBytes = Files.readAllBytes(tempFile);
//
//        return new MockMultipartFile(
//                "file",
//                fileName,
//                "image/" + format,
//                imageBytes
//        );
//    }
//}
