package com.recipemate.domain.user.dto;

import com.recipemate.domain.user.entity.Address;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AddressResponse {

    private Long id;
    private String addressName;
    private String recipientName;
    private String street;
    private String city;
    private String zipcode;
    private String recipientPhoneNumber;
    private Boolean isDefault;
    private String fullAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AddressResponse from(Address address) {
        return AddressResponse.builder()
            .id(address.getId())
            .addressName(address.getAddressName())
            .recipientName(address.getRecipientName())
            .street(address.getStreet())
            .city(address.getCity())
            .zipcode(address.getZipcode())
            .recipientPhoneNumber(address.getRecipientPhoneNumber())
            .isDefault(address.getIsDefault())
            .fullAddress(address.getFullAddress())
            .createdAt(address.getCreatedAt())
            .updatedAt(address.getUpdatedAt())
            .build();
    }
}
