package com.recipemate.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequest {

    @NotBlank(message = "배송지명은 필수입니다")
    @Size(max = 50, message = "배송지명은 50자 이하여야 합니다")
    private String addressName;

    @NotBlank(message = "수령인 이름은 필수입니다")
    @Size(max = 50, message = "수령인 이름은 50자 이하여야 합니다")
    private String recipientName;

    @NotBlank(message = "도로명 주소는 필수입니다")
    @Size(max = 200, message = "도로명 주소는 200자 이하여야 합니다")
    private String street;

    @NotBlank(message = "시/구는 필수입니다")
    @Size(max = 100, message = "시/구는 100자 이하여야 합니다")
    private String city;

    @NotBlank(message = "우편번호는 필수입니다")
    @Size(max = 10, message = "우편번호는 10자 이하여야 합니다")
    private String zipcode;

    @NotBlank(message = "수령인 전화번호는 필수입니다")
    @Size(max = 13, message = "수령인 전화번호는 13자 이하여야 합니다")
    private String recipientPhoneNumber;

    private Boolean isDefault;
}
