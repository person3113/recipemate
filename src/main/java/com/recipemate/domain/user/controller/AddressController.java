package com.recipemate.domain.user.controller;

import com.recipemate.domain.user.dto.AddressRequest;
import com.recipemate.domain.user.dto.AddressResponse;
import com.recipemate.domain.user.service.AddressService;
import com.recipemate.domain.user.service.CustomUserDetailsService.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/users/me/addresses")
@RequiredArgsConstructor
@Slf4j
public class AddressController {

    private final AddressService addressService;

    /**
     * 배송지 목록 페이지
     * GET /users/me/addresses
     */
    @GetMapping
    public String addressList(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        Model model
    ) {
        Long userId = userDetails.getUserId();
        List<AddressResponse> addresses = addressService.getUserAddresses(userId);
        
        model.addAttribute("addresses", addresses);
        return "user/addresses";
    }

    /**
     * 배송지 등록 페이지
     * GET /users/me/addresses/new
     */
    @GetMapping("/new")
    public String addressForm(Model model) {
        model.addAttribute("addressRequest", new AddressRequest());
        return "user/address-form";
    }

    /**
     * 배송지 등록 처리
     */
    @PostMapping("/new")
    public String createAddress(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @ModelAttribute AddressRequest request,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        log.info("배송지 등록 요청 - userId: {}, addressName: {}", 
            userDetails.getUserId(), request.getAddressName());
        
        // Validation 에러 체크
        if (bindingResult.hasErrors()) {
            log.warn("배송지 등록 Validation 실패 - userId: {}, errors: {}", 
                userDetails.getUserId(), bindingResult.getAllErrors());
            model.addAttribute("addressRequest", request);
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "user/address-form";
        }
        
        try {
            Long userId = userDetails.getUserId();
            AddressResponse savedAddress = addressService.createAddress(userId, request);
            
            log.info("배송지 등록 성공 - userId: {}, addressId: {}", userId, savedAddress.getId());
            redirectAttributes.addFlashAttribute("successMessage", "배송지가 등록되었습니다.");
            return "redirect:/users/me/addresses";
        } catch (Exception e) {
            log.error("배송지 등록 실패 - userId: {}, error: {}", 
                userDetails.getUserId(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "배송지 등록에 실패했습니다: " + e.getMessage());
            return "redirect:/users/me/addresses/new";
        }
    }

    /**
     * 배송지 수정 페이지
     * GET /users/me/addresses/{addressId}/edit
     */
    @GetMapping("/{addressId}/edit")
    public String editAddressForm(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long addressId,
        Model model
    ) {
        Long userId = userDetails.getUserId();
        AddressResponse address = addressService.getAddress(userId, addressId);
        
        model.addAttribute("address", address);
        model.addAttribute("addressRequest", AddressRequest.builder()
            .addressName(address.getAddressName())
            .recipientName(address.getRecipientName())
            .street(address.getStreet())
            .city(address.getCity())
            .zipcode(address.getZipcode())
            .recipientPhoneNumber(address.getRecipientPhoneNumber())
            .isDefault(address.getIsDefault())
            .build());
        return "user/address-form";
    }

    /**
     * 배송지 수정 처리
     * POST /users/me/addresses/{addressId}
     */
    @PostMapping("/{addressId}")
    public String updateAddress(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long addressId,
        @Valid @ModelAttribute AddressRequest request,
        RedirectAttributes redirectAttributes
    ) {
        Long userId = userDetails.getUserId();
        addressService.updateAddress(userId, addressId, request);
        
        redirectAttributes.addFlashAttribute("successMessage", "배송지가 수정되었습니다.");
        return "redirect:/users/me/addresses";
    }

    /**
     * 배송지 삭제
     * POST /users/me/addresses/{addressId}/delete
     */
    @PostMapping("/{addressId}/delete")
    public String deleteAddress(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long addressId,
        RedirectAttributes redirectAttributes
    ) {
        Long userId = userDetails.getUserId();
        addressService.deleteAddress(userId, addressId);
        
        redirectAttributes.addFlashAttribute("successMessage", "배송지가 삭제되었습니다.");
        return "redirect:/users/me/addresses";
    }

    /**
     * 기본 배송지 설정
     * POST /users/me/addresses/{addressId}/set-default
     */
    @PostMapping("/{addressId}/set-default")
    public String setDefaultAddress(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long addressId,
        RedirectAttributes redirectAttributes
    ) {
        Long userId = userDetails.getUserId();
        addressService.setDefaultAddress(userId, addressId);
        
        redirectAttributes.addFlashAttribute("successMessage", "기본 배송지로 설정되었습니다.");
        return "redirect:/users/me/addresses";
    }

    /**
     * htmx Fragment: 배송지 목록 조회 (AJAX)
     * GET /users/me/addresses/fragments/list
     */
    @GetMapping("/fragments/list")
    public String getAddressesFragment(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        Model model
    ) {
        Long userId = userDetails.getUserId();
        List<AddressResponse> addresses = addressService.getUserAddresses(userId);
        model.addAttribute("addresses", addresses);
        return "user/fragments/address-list";
    }

    /**
     * htmx Fragment: 배송지 선택 옵션 (공동구매 참여 폼용)
     * GET /users/me/addresses/fragments/options
     */
    @GetMapping("/fragments/options")
    public String getAddressOptionsFragment(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        Model model
    ) {
        Long userId = userDetails.getUserId();
        List<AddressResponse> addresses = addressService.getUserAddresses(userId);
        model.addAttribute("addresses", addresses);
        return "user/fragments/address-options";
    }
}
