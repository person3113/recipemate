package com.recipemate.domain.user.service;

import com.recipemate.domain.user.dto.AddressRequest;
import com.recipemate.domain.user.dto.AddressResponse;
import com.recipemate.domain.user.entity.Address;
import com.recipemate.domain.user.entity.User;
import com.recipemate.domain.user.repository.AddressRepository;
import com.recipemate.domain.user.repository.UserRepository;
import com.recipemate.global.exception.CustomException;
import com.recipemate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    /**
     * 사용자의 배송지 목록 조회
     */
    public List<AddressResponse> getUserAddresses(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Address> addresses = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
        return addresses.stream()
            .map(AddressResponse::from)
            .collect(Collectors.toList());
    }

    /**
     * 배송지 상세 조회
     */
    public AddressResponse getAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByUserIdAndAddressId(userId, addressId)
            .orElseThrow(() -> new CustomException(ErrorCode.ADDRESS_NOT_FOUND));

        return AddressResponse.from(address);
    }

    /**
     * 기본 배송지 조회
     */
    public AddressResponse getDefaultAddress(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Address address = addressRepository.findByUserAndIsDefaultTrue(user)
            .orElseThrow(() -> new CustomException(ErrorCode.ADDRESS_NOT_FOUND));

        return AddressResponse.from(address);
    }

    /**
     * 배송지 생성
     */
    @Transactional
    public AddressResponse createAddress(Long userId, AddressRequest request) {
        log.info("배송지 생성 시작 - userId: {}, addressName: {}", userId, request.getAddressName());
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        log.debug("사용자 조회 완료 - userId: {}, email: {}", user.getId(), user.getEmail());

        // 기본 배송지로 설정 시, 기존 기본 배송지 해제
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            log.debug("기본 배송지로 설정 요청됨 - 기존 기본 배송지 해제 시도");
            addressRepository.findByUserAndIsDefaultTrue(user)
                .ifPresent(existingDefault -> {
                    log.debug("기존 기본 배송지 발견 - addressId: {}", existingDefault.getId());
                    existingDefault.unsetAsDefault();
                });
        }

        // 첫 번째 배송지는 자동으로 기본 배송지로 설정
        boolean isDefault = request.getIsDefault() != null ? request.getIsDefault() : false;
        if (!addressRepository.existsByUserAndIsDefaultTrue(user)) {
            log.debug("사용자의 첫 번째 배송지 - 자동으로 기본 배송지로 설정");
            isDefault = true;
        }

        Address address = Address.create(
            user,
            request.getAddressName(),
            request.getRecipientName(),
            request.getStreet(),
            request.getCity(),
            request.getZipcode(),
            request.getRecipientPhoneNumber(),
            isDefault
        );
        
        log.debug("Address 엔티티 생성 완료 - addressName: {}, isDefault: {}", address.getAddressName(), address.getIsDefault());

        Address savedAddress = addressRepository.save(address);
        
        log.info("배송지 저장 완료 - addressId: {}, userId: {}, addressName: {}", 
            savedAddress.getId(), userId, savedAddress.getAddressName());
        
        return AddressResponse.from(savedAddress);
    }

    /**
     * 배송지 수정
     */
    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
        Address address = addressRepository.findByUserIdAndAddressId(userId, addressId)
            .orElseThrow(() -> new CustomException(ErrorCode.ADDRESS_NOT_FOUND));

        // 기본 배송지로 설정 시, 기존 기본 배송지 해제
        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.getIsDefault()) {
            User user = address.getUser();
            addressRepository.findByUserAndIsDefaultTrue(user)
                .ifPresent(Address::unsetAsDefault);
            address.setAsDefault();
        } else if (Boolean.FALSE.equals(request.getIsDefault()) && address.getIsDefault()) {
            // 기본 배송지를 해제하려는 경우, 최소 1개의 기본 배송지가 필요하므로 거부
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        address.update(
            request.getAddressName(),
            request.getRecipientName(),
            request.getStreet(),
            request.getCity(),
            request.getZipcode(),
            request.getRecipientPhoneNumber()
        );

        return AddressResponse.from(address);
    }

    /**
     * 배송지 삭제
     */
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByUserIdAndAddressId(userId, addressId)
            .orElseThrow(() -> new CustomException(ErrorCode.ADDRESS_NOT_FOUND));

        // 기본 배송지 삭제 시, 다른 배송지가 있으면 가장 최근 배송지를 기본으로 설정
        if (address.getIsDefault()) {
            User user = address.getUser();
            List<Address> otherAddresses = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user)
                .stream()
                .filter(a -> !a.getId().equals(addressId))
                .collect(Collectors.toList());

            if (!otherAddresses.isEmpty()) {
                otherAddresses.get(0).setAsDefault();
            }
        }

        addressRepository.delete(address);
    }

    /**
     * 기본 배송지 설정
     */
    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByUserIdAndAddressId(userId, addressId)
            .orElseThrow(() -> new CustomException(ErrorCode.ADDRESS_NOT_FOUND));

        // 기존 기본 배송지 해제
        User user = address.getUser();
        addressRepository.findByUserAndIsDefaultTrue(user)
            .ifPresent(Address::unsetAsDefault);

        // 새로운 기본 배송지 설정
        address.setAsDefault();
    }
}
