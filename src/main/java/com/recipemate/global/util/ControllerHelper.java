package com.recipemate.global.util;

import com.recipemate.global.exception.CustomException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.function.Supplier;

/**
 * 컨트롤러 공통 유틸리티
 * MVC 리다이렉트 + FlashAttributes 처리 헬퍼
 */
public class ControllerHelper {

    /**
     * 예외 처리와 FlashAttributes를 자동으로 처리하는 헬퍼 메서드
     * 
     * @param action 실행할 비즈니스 로직 (예외 발생 가능)
     * @param redirectAttributes 플래시 메시지를 추가할 RedirectAttributes
     * @param successMessage 성공 시 메시지
     * @param successRedirect 성공 시 리다이렉트 경로
     * @param errorRedirect 에러 시 리다이렉트 경로
     * @return 리다이렉트 경로 문자열
     */
    public static String handleWithRedirect(
            Runnable action,
            RedirectAttributes redirectAttributes,
            String successMessage,
            String successRedirect,
            String errorRedirect
    ) {
        try {
            action.run();
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:" + successRedirect;
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getErrorCode().getMessage());
            return "redirect:" + errorRedirect;
        }
    }

    /**
     * 예외 처리와 FlashAttributes를 자동으로 처리하는 헬퍼 메서드 (반환값 있는 버전)
     * 
     * @param action 실행할 비즈니스 로직 (결과 반환)
     * @param redirectAttributes 플래시 메시지를 추가할 RedirectAttributes
     * @param successMessage 성공 시 메시지
     * @param successRedirectSupplier 성공 시 리다이렉트 경로 (결과값 사용 가능)
     * @param errorRedirect 에러 시 리다이렉트 경로
     * @return 리다이렉트 경로 문자열
     */
    public static <T> String handleWithRedirect(
            Supplier<T> action,
            RedirectAttributes redirectAttributes,
            String successMessage,
            java.util.function.Function<T, String> successRedirectSupplier,
            String errorRedirect
    ) {
        try {
            T result = action.get();
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:" + successRedirectSupplier.apply(result);
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getErrorCode().getMessage());
            return "redirect:" + errorRedirect;
        }
    }

    /**
     * 에러 메시지 추가 헬퍼
     */
    public static void addErrorMessage(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("errorMessage", message);
    }

    /**
     * 성공 메시지 추가 헬퍼
     */
    public static void addSuccessMessage(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("successMessage", message);
    }
}
