package com.recipemate.domain.user.entity;

import com.recipemate.global.common.BaseEntity;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "addresses", indexes = {
    @Index(name = "idx_address_user_id", columnList = "user_id")
})
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Address extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_address_user"))
    private User user;

    @Column(nullable = false, length = 50)
    private String addressName;

    @Column(nullable = false, length = 50)
    private String recipientName;

    @Column(nullable = false, length = 200)
    private String street;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 10)
    private String zipcode;

    @Column(nullable = false, length = 13)
    private String recipientPhoneNumber;

    @Column(nullable = false)
    private Boolean isDefault;

    //== 생성 메서드 ==//
    public static Address create(
        User user, 
        String addressName, 
        String recipientName,
        String street, 
        String city, 
        String zipcode, 
        String recipientPhoneNumber,
        Boolean isDefault
    ) {
        validateCreateArgs(addressName, recipientName, street, city, zipcode, recipientPhoneNumber);
        
        return Address.builder()
            .user(user)
            .addressName(addressName)
            .recipientName(recipientName)
            .street(street)
            .city(city)
            .zipcode(zipcode)
            .recipientPhoneNumber(recipientPhoneNumber)
            .isDefault(isDefault != null ? isDefault : false)
            .build();
    }

    private static void validateCreateArgs(
        String addressName,
        String recipientName,
        String street,
        String city,
        String zipcode,
        String recipientPhoneNumber
    ) {
        if (addressName == null || addressName.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (recipientName == null || recipientName.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (street == null || street.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (city == null || city.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (zipcode == null || zipcode.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (recipientPhoneNumber == null || recipientPhoneNumber.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    //== 수정 메서드 ==//
    public void update(
        String addressName,
        String recipientName,
        String street,
        String city,
        String zipcode,
        String recipientPhoneNumber
    ) {
        validateCreateArgs(addressName, recipientName, street, city, zipcode, recipientPhoneNumber);
        
        this.addressName = addressName;
        this.recipientName = recipientName;
        this.street = street;
        this.city = city;
        this.zipcode = zipcode;
        this.recipientPhoneNumber = recipientPhoneNumber;
    }

    public void setAsDefault() {
        this.isDefault = true;
    }

    public void unsetAsDefault() {
        this.isDefault = false;
    }

    //== 편의 메서드 ==//
    public String getFullAddress() {
        return String.format("[%s] %s %s (%s)", zipcode, city, street, addressName);
    }
}
