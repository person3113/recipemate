package com.recipemate.domain.post.dto;

import com.recipemate.global.common.PostCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 5000, message = "내용은 5000자 이하여야 합니다")
    private String content;

    @NotNull(message = "카테고리는 필수입니다")
    private PostCategory category;
    
    @Builder.Default
    private List<MultipartFile> imageFiles = new ArrayList<>();
}

