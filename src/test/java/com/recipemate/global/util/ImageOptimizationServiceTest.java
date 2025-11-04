package com.recipemate.global.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ImageOptimizationService 테스트")
class ImageOptimizationServiceTest {

    private ImageOptimizationService imageOptimizationService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        imageOptimizationService = new ImageOptimizationService();
    }

    @Test
    @DisplayName("이미지를 최적화하여 유효한 이미지를 생성한다")
    void optimizeImage_CreatesValidImage() throws IOException {
        // Given: 큰 이미지 파일 생성
        MockMultipartFile originalImage = createMockImage(1920, 1080, "test.jpg");

        // When: 이미지 최적화 수행
        byte[] optimizedImageData = imageOptimizationService.optimizeImage(
                originalImage.getBytes(),
                "test.jpg"
        );

        // Then: 최적화된 이미지가 유효하고 크기가 적절해야 함
        assertThat(optimizedImageData).isNotNull();
        assertThat(optimizedImageData.length).isGreaterThan(0);
        
        // 최적화된 이미지가 실제로 읽을 수 있는 유효한 이미지인지 확인
        BufferedImage resultImage = ImageIO.read(new ByteArrayInputStream(optimizedImageData));
        assertThat(resultImage).isNotNull();
        assertThat(resultImage.getWidth()).isLessThanOrEqualTo(1920);
        assertThat(resultImage.getHeight()).isLessThanOrEqualTo(1080);
    }

    @Test
    @DisplayName("큰 이미지를 지정된 최대 크기로 리사이즈한다")
    void optimizeImage_ResizesLargeImage() throws IOException {
        // Given: 매우 큰 이미지
        MockMultipartFile largeImage = createMockImage(3000, 2000, "large.jpg");

        // When: 최대 크기 1920x1920으로 최적화
        byte[] optimizedImageData = imageOptimizationService.optimizeImage(
                largeImage.getBytes(),
                "large.jpg"
        );

        // Then: 이미지 크기가 최대 크기 이하로 조정됨
        BufferedImage resultImage = ImageIO.read(new ByteArrayInputStream(optimizedImageData));
        assertThat(resultImage.getWidth()).isLessThanOrEqualTo(1920);
        assertThat(resultImage.getHeight()).isLessThanOrEqualTo(1920);
    }

    @Test
    @DisplayName("작은 이미지는 리사이즈하지 않고 압축만 수행한다")
    void optimizeImage_SmallImageNotResized() throws IOException {
        // Given: 작은 이미지
        int originalWidth = 800;
        int originalHeight = 600;
        MockMultipartFile smallImage = createMockImage(originalWidth, originalHeight, "small.jpg");

        // When: 최적화 수행
        byte[] optimizedImageData = imageOptimizationService.optimizeImage(
                smallImage.getBytes(),
                "small.jpg"
        );

        // Then: 이미지 크기는 유지되고 압축만 수행
        BufferedImage resultImage = ImageIO.read(new ByteArrayInputStream(optimizedImageData));
        assertThat(resultImage.getWidth()).isEqualTo(originalWidth);
        assertThat(resultImage.getHeight()).isEqualTo(originalHeight);
    }

    @Test
    @DisplayName("썸네일 이미지를 생성한다")
    void createThumbnail_CreatesSmallImage() throws IOException {
        // Given: 원본 이미지
        MockMultipartFile originalImage = createMockImage(1920, 1080, "original.jpg");

        // When: 썸네일 생성 (300x300)
        byte[] thumbnailData = imageOptimizationService.createThumbnail(
                originalImage.getBytes(),
                300,
                300
        );

        // Then: 썸네일이 지정된 크기로 생성됨
        BufferedImage thumbnail = ImageIO.read(new ByteArrayInputStream(thumbnailData));
        assertThat(thumbnail.getWidth()).isLessThanOrEqualTo(300);
        assertThat(thumbnail.getHeight()).isLessThanOrEqualTo(300);
        assertThat(thumbnailData.length).isLessThan((int) originalImage.getSize());
    }

    @Test
    @DisplayName("썸네일 생성 시 비율을 유지한다")
    void createThumbnail_MaintainsAspectRatio() throws IOException {
        // Given: 가로 비율이 긴 이미지 (16:9)
        MockMultipartFile wideImage = createMockImage(1600, 900, "wide.jpg");

        // When: 정사각형 썸네일 생성 시도
        byte[] thumbnailData = imageOptimizationService.createThumbnail(
                wideImage.getBytes(),
                300,
                300
        );

        // Then: 비율이 유지되어야 함 (가로가 300, 세로는 더 작음)
        BufferedImage thumbnail = ImageIO.read(new ByteArrayInputStream(thumbnailData));
        double originalRatio = 1600.0 / 900.0;
        double thumbnailRatio = (double) thumbnail.getWidth() / thumbnail.getHeight();
        assertThat(thumbnailRatio).isCloseTo(originalRatio, within(0.1));
    }

    @Test
    @DisplayName("WebP 형식으로 변환한다")
    void convertToWebP_ConvertsSuccessfully() throws IOException {
        // Given: JPEG 이미지
        MockMultipartFile jpegImage = createMockImage(1920, 1080, "image.jpg");

        // When: WebP로 변환 (현재는 고품질 JPEG로 변환)
        byte[] convertedData = imageOptimizationService.convertToWebP(jpegImage.getBytes());

        // Then: 변환이 성공하고 유효한 이미지가 생성됨
        assertThat(convertedData).isNotNull();
        assertThat(convertedData.length).isGreaterThan(0);
        
        // 변환된 이미지가 유효한지 확인
        BufferedImage resultImage = ImageIO.read(new ByteArrayInputStream(convertedData));
        assertThat(resultImage).isNotNull();
        assertThat(resultImage.getWidth()).isEqualTo(1920);
        assertThat(resultImage.getHeight()).isEqualTo(1080);
    }

    @Test
    @DisplayName("PNG 이미지도 최적화할 수 있다")
    void optimizeImage_SupportsPngFormat() throws IOException {
        // Given: PNG 이미지
        MockMultipartFile pngImage = createMockImage(1920, 1080, "image.png");

        // When: 최적화 수행
        byte[] optimizedData = imageOptimizationService.optimizeImage(
                pngImage.getBytes(),
                "image.png"
        );

        // Then: PNG 형식으로 최적화됨
        assertThat(optimizedData).isNotNull();
        assertThat(optimizedData.length).isGreaterThan(0);
        BufferedImage resultImage = ImageIO.read(new ByteArrayInputStream(optimizedData));
        assertThat(resultImage).isNotNull();
    }

    @Test
    @DisplayName("잘못된 이미지 데이터는 예외를 발생시킨다")
    void optimizeImage_InvalidImageThrowsException() {
        // Given: 잘못된 이미지 데이터
        byte[] invalidData = "not an image".getBytes();

        // When & Then: 예외 발생
        assertThatThrownBy(() ->
                imageOptimizationService.optimizeImage(invalidData, "invalid.jpg")
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미지");
    }

    @Test
    @DisplayName("null 입력은 예외를 발생시킨다")
    void optimizeImage_NullInputThrowsException() {
        // When & Then
        assertThatThrownBy(() ->
                imageOptimizationService.optimizeImage(null, "test.jpg")
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("빈 파일명은 예외를 발생시킨다")
    void optimizeImage_EmptyFileNameThrowsException() throws IOException {
        // Given
        MockMultipartFile image = createMockImage(800, 600, "test.jpg");

        // When & Then
        assertThatThrownBy(() ->
                imageOptimizationService.optimizeImage(image.getBytes(), "")
        ).isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * 테스트용 Mock 이미지 생성
     */
    private MockMultipartFile createMockImage(int width, int height, String fileName) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // 이미지에 패턴 추가 (압축 효과를 보기 위해)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = ((x + y) % 256) << 16 | ((x * 2) % 256) << 8 | ((y * 2) % 256);
                image.setRGB(x, y, rgb);
            }
        }

        // 임시 파일로 저장 후 읽기
        Path tempFile = tempDir.resolve(fileName);
        String format = fileName.endsWith(".png") ? "png" : "jpg";
        ImageIO.write(image, format, tempFile.toFile());
        byte[] imageBytes = Files.readAllBytes(tempFile);

        return new MockMultipartFile(
                "file",
                fileName,
                "image/" + format,
                imageBytes
        );
    }
}
